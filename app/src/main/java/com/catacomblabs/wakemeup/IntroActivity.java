package com.catacomblabs.wakemeup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro2;

public class IntroActivity extends AppIntro2 {

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        addSlide(SampleSlide.newInstance(R.layout.intro_page_1));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            addSlide(SampleSlide.newInstance(R.layout.intro_page_2));
            askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        addSlide(SampleSlide.newInstance(R.layout.intro_page_3));
        addSlide(SampleSlide.newInstance(R.layout.intro_page_4));
        addSlide(SampleSlide.newInstance(R.layout.intro_page_5));

        setCustomTransformer(new ZoomOutPageTransformer());
    }

    @Override
    public void onSlideChanged() {
    }

    @Override
    public void onDonePressed() {
        Intent intent = new Intent(this, MainMenuRailway.class);
        intent.putExtra("dontShowIntro", false);
        startActivity(intent);
    }

    @Override
    public void onNextPressed() {
    }

    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        boolean allowExit = intent.getBooleanExtra("startedFromMenu", false);
        if (allowExit)
            super.onBackPressed();
    }
}
