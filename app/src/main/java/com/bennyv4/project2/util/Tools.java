package com.bennyv4.project2.util;

import android.content.*;
import android.content.res.*;
import android.util.*;
import android.widget.Toast;

public class Tools
{
	private Tools(){}
	
	public static float convertDpToPixel(float dp, Context context){
		Resources resources = context.getResources();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
	}

	public static void toast(Context context,String str){
        Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context,int str){
        Toast.makeText(context,context.getResources().getString(str),Toast.LENGTH_SHORT).show();
    }
}
