package demo.yl.sensor.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import demo.yl.sensor.R;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by Yl on 16/11/29.
 */

public class SplashActivity extends AppCompatActivity {
    ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiivty_splash);
         iv= (ImageView) findViewById(R.id.splash);
        redirect();
//        Glide.with(this).load(Application.splashUrl).placeholder(R.mipmap.splash).into(new GlideDrawableImageViewTarget(iv){
//            @Override
//            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
//                super.onResourceReady(resource, animation);
//                yLog.e("加载成功");
//
//                    redirect();
////                ReentrantLock
////                AtomicInteger<Thread> ar=new AtomicReference<Thread>();
////               Field[] ff= SplashActivity.class.get();
////                for(int i=0;i<ff.length;i++){
////                    yLog.e(ff[i].toString());
////                }
//            }
//
//            @Override
//            public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                super.onLoadFailed(e, errorDrawable);
//                redirect();
//                yLog.e("加载失败");
//
//            }
//        });

    }

    private void redirect()
    {
//        startActivity(new Intent(this,MainActivity.class));

//        Intent i=new Intent();
//        i.setClass(SplashActivity.this,MainActivity.class);
//        startActivity(i);
//        finish();
        Observable.timer(2, java.util.concurrent.TimeUnit.SECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        jump();
                    }
                });
    }
    private void jump()
    {
        Intent i = new Intent();
        i.setClass(SplashActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
