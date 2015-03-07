package com.lemonslice.appathon;

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

    class bounding
    {
        int x, y, width, height;

        bounding(int x, int y, int width, int height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    bounding boundings[] =
    {
            new bounding(EYE_BOX_LEFT_X, EYE_BOX_Y, EYE_BOX_WIDTH, EYE_BOX_HEIGHT),
            new bounding(EYE_BOX_RIGHT_X, EYE_BOX_Y, EYE_BOX_WIDTH, EYE_BOX_HEIGHT),
            new bounding(MOUTH_BOX_X, MOUTH_BOX_Y, MOUTH_BOX_WIDTH, MOUTH_BOX_HEIGHT)
    };

    String images[] =
    {
            "smile.jpg",
            "smile_super.jpg",
            "sunglasses.jpg",
            "tongue.jpg",
            "tongue_wink.jpg",
            "wink_smile.jpg",
            "wink_smile_super.jpg"
    };

    byte[] image_mat;

    class rgb
    {
        float r, g, b;

        rgb(float val)
        {
            r = val;
            g = val;
            b = val;
        }
    }

    rgb feature_vals[] =
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

    state[] feature_states =
    {
            state.OPEN,
            state.CLOSED,
            state.CLOSED,
            state.TONGUE,
            state.OPEN,
            state.SUNGLASSES
    };

    feature_type[] types =
    {
        feature_type.EYES,
        feature_type.EYES,
        feature_type.MOUTH,
        feature_type.MOUTH,
        feature_type.MOUTH,
        feature_type.EYES
    };

    emoji map_emoji(int features[])
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

}
