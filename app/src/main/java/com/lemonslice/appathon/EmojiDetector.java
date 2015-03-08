package com.lemonslice.appathon;

import android.graphics.YuvImage;
import android.util.Log;
import android.graphics.Rect;


/**
 * Created by jamesburnside on 07/03/15.
 *
 * go me, go me, yeah yeah yeha
 */
final public class EmojiDetector {

    static int MOUTH_BOX_WIDTH = 200;
    static int MOUTH_BOX_HEIGHT = 160;
    static int MOUTH_BOX_X = 400;
    static int MOUTH_BOX_Y = 780;

    static int EYE_BOX_WIDTH = 130;
    static int EYE_BOX_HEIGHT=  90;
    static int EYE_BOX_LEFT_X = 330;
    static int EYE_BOX_RIGHT_X = 540;
    static int EYE_BOX_Y = 565;

    static int FEATURES = 3;

    enum emoji
    {
        E_NONE,
        E_SMILE,
        E_SUPER_SMILE,
        E_TONGUE,
        E_WINK,
        E_SUPER_WINK,
        E_TONGUE_WINK,
        E_SUNGLASSES
    }

    enum state
    {
        OPEN,
        CLOSED,
        TONGUE,
        SUNGLASSES
    }

    enum feature_type
    {
        EYES,
        MOUTH
    }

    String base = "jamesimages/";

    static class bounding
    {
        int x, y, width, height;

        bounding(int x, int y, int width, int height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        bounding(float x, float y, float width, float height)
        {
            this.x = (int)x;
            this.y = (int)y;
            this.width = (int)width;
            this.height = (int)height;
        }
    }


    static String images[] =
    {
        "smile.jpg",
        "smile_super.jpg",
        "sunglasses.jpg",
        "tongue.jpg",
        "tongue_wink.jpg",
        "wink_smile.jpg",
        "wink_smile_super.jpg"
    };

    static byte[] image_mat;

    static class rgb
    {
        float r, g, b;

        rgb(float val)
        {
            r = val;
            g = val;
            b = val;
        }

        rgb(float x, float y, float z)
        {
            r = x;
            g = y;
            b = z;
        }
    }

    static rgb feature_vals[] =
    {
        ///Eyes open
        new rgb(0),
        ///Eyes closed
        new rgb(0),
        ///Mouth closed
        new rgb(0),
        ///Tongue out
        new rgb(0),
        ///Mouth open
        new rgb(0),
        ///Sunglasses
        new rgb(0)
    };

    static state[] feature_states =
    {
        state.OPEN,
        state.CLOSED,
        state.CLOSED,
        state.TONGUE,
        state.OPEN,
        state.SUNGLASSES
    };

    static feature_type[] types =
    {
        feature_type.EYES,
        feature_type.EYES,
        feature_type.MOUTH,
        feature_type.MOUTH,
        feature_type.MOUTH,
        feature_type.EYES
    };

    static emoji map_emoji(int features[])
    {
        state eye1_state, eye2_state, mouth_state;

        eye1_state=feature_states[features[0]];
        eye2_state=feature_states[features[1]];
        mouth_state=feature_states[features[2]];

        //if one is sunglasses and one isn't, assign both to be the opposite one
        if(eye1_state != eye2_state)
        {
            if(eye1_state == state.SUNGLASSES)
                eye1_state = eye2_state;
            else if(eye2_state == state.SUNGLASSES)
                eye2_state = eye1_state;
        }

        if(mouth_state == state.OPEN)
        {
            //wink - if both different or both closed
            if(eye1_state != eye2_state || (eye1_state == state.CLOSED))
            {
                return emoji.E_SUPER_WINK;
            }
            //both open
            else if(eye1_state == state.OPEN)
            {
                return emoji.E_SUPER_SMILE;
            }
            //both sunglasses
            else if(eye1_state == state.SUNGLASSES)
            {
                return emoji.E_SUNGLASSES;
            }
        }
        else if(mouth_state == state.CLOSED)
        {
            //wink - if both different or both closed
            if(eye1_state != eye2_state || (eye1_state == state.CLOSED))
            {
                return emoji.E_WINK;
            }
            //both open
            else if(eye1_state == state.OPEN)
            {
                return emoji.E_SMILE;
            }
            //both sunglasses
            else if(eye1_state == state.SUNGLASSES)
            {
                return emoji.E_SUNGLASSES;
            }
        }
        else if(mouth_state == state.TONGUE)
        {
            //wink - if both different or both closed
            if(eye1_state != eye2_state || (eye1_state == state.CLOSED))
            {
                return emoji.E_TONGUE_WINK;
            }
            //both open
            else if(eye1_state == state.OPEN)
            {
                return emoji.E_TONGUE;
            }
            //both sunglasses
            else if(eye1_state == state.SUNGLASSES)
            {
                return emoji.E_SUNGLASSES;
            }
        }

        assert(false); //how'd we even get here??
        return emoji.E_NONE;
    }

    static{
        feature_vals[0] = new rgb(84.782310f, 85.723679f, 126.453461f);
        feature_vals[1] = new rgb(48.754528f, 55.510853f, 96.772141f);
        feature_vals[2] = new rgb(102.402092f, 104.084221f, 163.292557f);
        feature_vals[3] = new rgb(92.549904f, 87.093628f, 152.024277f);
        feature_vals[4] = new rgb(76.390968f, 79.286438f, 130.261307f);
        feature_vals[5] = new rgb(0.000000f, 0.000000f, 0.000000f);
    }

    static rgb yuv_to_rgb(byte[] data, int width, int height, int x, int y)
    {
        /// pixel values and widths are hardcoded, this is terribad
        if(y >= height || x >= width)
            return new rgb(0);

        if(x < 0 || y < 0)
            return new rgb(0);

        if(y*width + x >= data.length)
            return new rgb(0);

        int Y = data[y*width + x] & 0xff;

        data[y*width + x] = 0;

        // Get U and V values, stored after Y values, one per 2x2 block
        // of pixels, interleaved. Prepare them as floats with correct range
        // ready for calculation later.
        int xby2 = x/2;
        int yby2 = y/2;

        // make this V for NV12/420SP
        // pixel values hardcoded
        if(width*height + 2*xby2 + 1 + yby2*width >= data.length)
            return new rgb(0);

        float U = (float)(data[width*height + 2*xby2 + yby2*width] & 0xff) - 128.0f;

        // make this U for NV12/420SP
        float V = (float)(data[width*height + 2*xby2 + 1 + yby2*width] & 0xff) - 128.0f;

        // Do the YUV -> RGB conversion
        float Yf = 1.164f*((float)Y) - 16.0f;
        int R = (int)(Yf + 1.596f*V);
        int G = (int)(Yf - 0.813f*V - 0.391f*U);
        int B = (int)(Yf            + 2.018f*U);

        // Clip rgb values to 0-255
        R = R < 0 ? 0 : R > 255 ? 255 : R;
        G = G < 0 ? 0 : G > 255 ? 255 : G;
        B = B < 0 ? 0 : B > 255 ? 255 : B;

        rgb val = new rgb(R, G, B);

        return val;
    }

    static rgb get_sum_val(byte[] data, int width, int height, int type, Rect rect)
    {
        /*bounding boundings[] =
        {
            new bounding(EYE_BOX_LEFT_X, EYE_BOX_Y, EYE_BOX_WIDTH, EYE_BOX_HEIGHT),
            new bounding(EYE_BOX_RIGHT_X, EYE_BOX_Y, EYE_BOX_WIDTH, EYE_BOX_HEIGHT),
            new bounding(MOUTH_BOX_X, MOUTH_BOX_Y, MOUTH_BOX_WIDTH, MOUTH_BOX_HEIGHT)
        };*/

        Rect lessretarded = new Rect(rect);

        Log.d("Facebits", "two " + String.valueOf(lessretarded.left) + " " + String.valueOf(lessretarded.top)
                + " " + String.valueOf(lessretarded.right) + " " + String.valueOf(lessretarded.bottom));

        float pl, pr, pt, pb;

        pl = lessretarded.left;
        pr = lessretarded.right;
        pt = lessretarded.top;
        pb = lessretarded.bottom;

        pl += 1000.f;
        pl /= 2000.f;

        pr += 1000.f;
        pr /= 2000.f;

        pt += 1000.f;
        pt /= 2000.f;

        pb += 1000.f;
        pb /= 2000.f;

        pb *= height;
        pr *= width;

        pl *= width;
        pt *= height;

        Log.d("Facebits", String.valueOf(pl) + " " + String.valueOf(pt)
                + " " + String.valueOf(pr) + " " + String.valueOf(pb));

        ///percentages
        float eyesize = 0.1f;
        float mouthWidth = 0.1f;
        float mouthHeight = 0.2f;

        float eye1offsety = 0.1f;
        float eye2offsety = eye1offsety + eyesize + 0.1f;

        float eye1offsetx = 0.8f;
        float eye2offsetx = eye1offsetx;

        float mouthoffsetx = 0.7f;
        float mouthoffsety = eye1offsety + eyesize/2.f;

        float mouthstartx = mouthoffsetx * width;
        float mouthstarty = mouthoffsety * height;

        float mouthwidth = eyesize * height;
        float mouthheight = 0.3f * height;

        float eye1startx = eye1offsetx * width;
        float eye2startx = eye2offsetx * width;

        float eye1starty = eye1offsety * height;
        float eye2starty = eye2offsety * height;

        float eyereal = eyesize * width;



        eye1starty = pt;
        eye2starty = pb - eyereal;

        eye1startx = (pr + pl)/2;
        eye2startx = (pr + pl)/2;

        mouthstartx = pl;
        mouthstarty = pt;

        float mouthendy = mouthstarty + mouthheight;

        float mouthextray = pb - mouthendy;

        mouthstarty += mouthextray / 2.f;




        Log.d("James", String.valueOf(width) + " " + String.valueOf(eye1startx) + " " + String.valueOf(eyereal));


        bounding boundings[] =
        {
                new bounding(eye1startx, eye1starty, eyereal, eyereal),
                new bounding(eye2startx, eye2starty, eyereal, eyereal),
                new bounding(mouthstartx, mouthstarty, mouthwidth, mouthheight)
        };

        rgb sum = new rgb(0);
        int num = 0;

        assert(type >= 0 && type < boundings.length);

        for(int j=boundings[type].y; j<boundings[type].y + boundings[type].height; j++)
        {
            for(int i=boundings[type].x; i<boundings[type].x + boundings[type].width; i++)
            {
                rgb val = yuv_to_rgb(data, width, height, i, j);

                //mat.at<Vec3b>(j, i) = 0;

                //sum += val;

                sum.r += val.r;
                sum.g += val.g;
                sum.b += val.b;

                num++;
            }
        }

        assert(num != 0);

        sum.r /= num;
        sum.g /= num;
        sum.b /= num;

        return sum;
    }

    static emoji get_emoji_from_image(YuvImage img, int width, int height, Rect rect)
    {
        byte[] data = img.getYuvData();


        int feature_nums[] = new int[FEATURES];

        for(int i=0; i<FEATURES; i++)
        {
            rgb feat = get_sum_val(data, width, height, i, rect);

            //printf("%f %f %f\n", feat[0], feat[1], feat[2]);

            int type = 0;

            if(i == 2)
                type = 1;

            float minimum_difference = Float.MAX_VALUE;
            int minimum_num = -1;

            for(int j=0; j<feature_vals.length; j++)
            {
                if(types[j].ordinal() != type)
                    continue;

                rgb my_val = feat;

                rgb their_val = feature_vals[j];

                float xd = their_val.r - my_val.r;
                float yd = their_val.g - my_val.g;
                float zd = their_val.b - my_val.b;

                float difference = xd*xd + yd*yd + zd*zd;

                difference = (float)Math.sqrt(difference);

                if(difference < minimum_difference)
                {
                    minimum_difference = difference;
                    minimum_num = j;
                }
            }

            if(minimum_num == -1)
                Log.d("James", "uh ohhhh");

            //printf("Feature %i classified as image %s\n", i, images[inverse_feature_vals[minimum_num]].c_str());

            //printf("Feature %i min %i classified as %i\n", i, minimum_num, feature_states[minimum_num]);

            feature_nums[i] = minimum_num;
        }

        emoji etype = map_emoji(feature_nums);

        Log.d("Test", etype.toString());

        return etype;
    }

}
