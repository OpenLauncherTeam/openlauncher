package com.bennnyv5.materialpreffragment;

import android.content.*;
import android.util.*;
import java.util.*;
import android.view.*;
import android.widget.*;
import android.support.v7.app.*;

public class CommonUtility
{
	public static int pixelToDp(Context ctx,int in){
		int out = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,in,ctx.getResources().getDisplayMetrics());
		return out;
	}
	
	public static String warpColorTag(String in ,int color){
		return "<font color="+String.format("#%06X", 0xFFFFFF & color)+">"+in+"</font>";
	}
	
	public static ViewGroup.LayoutParams matchParentLayoutParams(){
		return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
	}
	
	public static ViewGroup.LayoutParams matchParentWidthLayoutParams(){
		return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
	}
}
