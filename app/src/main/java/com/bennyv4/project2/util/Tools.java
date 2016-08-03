package com.bennyv4.project2.util;

import android.animation.Animator;
import android.content.*;
import android.content.res.*;
import android.os.Handler;
import android.util.*;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.bennyv4.project2.R;

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

    public static void startApp(Context c,AppManager.App app){
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(app.packageName,app.className);
        try {
            c.startActivity(intent);
        }catch (Exception e){
            Tools.toast(c, R.string.headsup_appuninstalled);
        }
    }

	public static void createScaleInScaleOutAnim(final View view, final Runnable endAction){
        view.animate().scaleX(0.85f).scaleY(0.85f).setDuration(80).setInterpolator(new AccelerateDecelerateInterpolator());
        new Handler().postDelayed(new Runnable() {
            public void run() {
                view.animate().scaleX(1f).scaleY(1f).setDuration(80).setInterpolator(new AccelerateDecelerateInterpolator());
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        endAction.run();
                    }
                }, 80);
            }
        }, 80);
    }
}
