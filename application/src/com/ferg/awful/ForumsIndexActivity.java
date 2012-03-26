/********************************************************************************
 * Copyright (c) 2011, Scott Ferguson
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the software nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SCOTT FERGUSON ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SCOTT FERGUSON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package com.ferg.awful;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Window;
import com.ferg.awful.constants.Constants;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ForumsIndexActivity extends AwfulActivity {

    private boolean DEVELOPER_MODE = false;
    private static final String TAG = "ForumsIndexActivity";

    private boolean mSecondPane;
    private ForumsIndexFragment mIndexFragment = null;
    private ForumDisplayFragment mForumFragment = null;
    private ThreadDisplayFragment mThreadFragment = null;
    private boolean skipLoad = false;
    
    private ViewPager mViewPager;
    private ForumPagerAdapter pagerAdapter;
    
    private int mForumId = 0;
    private int mThreadId = 0;
    private int mThreadPage = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        
        new Thread(new Runnable() {
            public void run() {
                GoogleAnalyticsTracker.getInstance().trackPageView("/ForumsIndexActivity");
                GoogleAnalyticsTracker.getInstance().dispatch();
            }
        }).start();
        if (isTV()) {
            startTVActivity();
        }else{
            requestWindowFeature(Window.FEATURE_ACTION_BAR);
	        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	        
	        mForumId = getIntent().getIntExtra(Constants.FORUM_ID, 0);
	        mThreadId = getIntent().getIntExtra(Constants.THREAD_ID, 0);
	        mThreadPage = getIntent().getIntExtra(Constants.THREAD_PAGE, 1);
	        
	        if(mForumId < 1){
	        	skipLoad = true;
	        }
	        
	        setContentView(R.layout.forum_index_activity);
	        mSecondPane = (findViewById(R.id.content)!= null);
	        setSupportProgressBarIndeterminateVisibility(false);
	        
	        if(isDualPane()){
		        mIndexFragment = (ForumsIndexFragment) getSupportFragmentManager().findFragmentById(R.id.forums_index);
		        if(mForumId > 0){
		        	setContentPane(mForumId);
		        }
	        }else{
	        	mViewPager = (ViewPager) findViewById(R.id.forum_index_pager);
	        	pagerAdapter = new ForumPagerAdapter(getSupportFragmentManager()); 
		        mViewPager.setAdapter(pagerAdapter);
		        mViewPager.setOnPageChangeListener(pagerAdapter);
		        if(mForumId > 0){
		        	mViewPager.setCurrentItem(1);
		        }else{
		        	mForumId = Constants.USERCP_ID;
		        }
		        if(mThreadId > 0){
		        	mViewPager.setCurrentItem(2);
		        }
		        Uri data = getIntent().getData();
		        if(data != null && (data.getLastPathSegment().contains("usercp") || data.getLastPathSegment().contains("forumdisplay") || data.getLastPathSegment().contains("bookmarkthreads"))){
		        	mViewPager.setCurrentItem(1);
		        }
	        }
	        if(mIndexFragment != null && mForumId > 0){
	        	mIndexFragment.setSelected(mForumId);
	        }
	        
	        setActionBar();
	        
	        checkIntentExtras();
        }
    }

    private void setActionBar() {
        ActionBar action = getSupportActionBar();
        action.setBackgroundDrawable(getResources().getDrawable(R.drawable.bar));
        action.setTitle(R.string.forums_title);
        action.setDisplayHomeAsUpEnabled(true);
    }

    private void checkIntentExtras() {
        if (getIntent().hasExtra(Constants.SHORTCUT)) {
            if (getIntent().getBooleanExtra(Constants.SHORTCUT, false)) {
            	setContentPane(Constants.USERCP_ID);
            }
        }
    }
    
    public class ForumPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener{
    	private int tabCount = 3;
		public ForumPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			Log.e(TAG,"CREATING TAB:"+arg0);
			switch(arg0){
			case 0:
				mIndexFragment = ForumsIndexFragment.newInstance();
				if(mForumId > 0){
					mIndexFragment.setSelected(mForumId);
				}
				return mIndexFragment;
			case 1:
				mForumFragment = ForumDisplayFragment.newInstance(mForumId, skipLoad);
				return mForumFragment;
			case 2:
				mThreadFragment = ThreadDisplayFragment.newInstance(mThreadId, mThreadPage);
				return mThreadFragment;
			default:
				Log.e(TAG,"TAB COUNT OUT OF BOUNDS");
			}
			return null;
		}

		@Override
		public int getCount() {
			return tabCount;
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int arg0) {
			switch(arg0){
			case 0:
				setActionbarTitle(getString(R.string.forums_title));
				break;
			case 1:
				if(mForumFragment != null && mForumFragment.getTitle() != null){
					setActionbarTitle(mForumFragment.getTitle());
					mForumFragment.syncForumsIfStale();
				}
				break;
			case 2:
				if(mThreadFragment != null && mThreadFragment.getTitle() != null){
					setActionbarTitle(mThreadFragment.getTitle());
				}
				break;
			default:
				Log.e(TAG,"TAB COUNT OUT OF BOUNDS");
			}
		}
    	
    }
    
    @Override
    public void onBackPressed() {
    	if(mViewPager != null && mViewPager.getCurrentItem() > 0){
    		mViewPager.setCurrentItem(mViewPager.getCurrentItem()-1);
    	}else{
    		finish();
    	}
        return;
    }

    private boolean isDualPane() {
        return mSecondPane;
    }
    
    @Override
    public void displayForum(int id, int page){
    	setContentPane(id);
    	if (!isDualPane()) {
    		mViewPager.setCurrentItem(1);
        }
    }

    public void setContentPane(int aForumId) {
    	mForumId = aForumId;
        if(mForumFragment == null && isDualPane()){
            ForumDisplayFragment fragment = ForumDisplayFragment.newInstance(aForumId, false);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        	transaction.add(R.id.content, fragment);
            transaction.commit();
        	mForumFragment = fragment;
        }else if(mForumFragment != null){
        	mForumFragment.openForum(aForumId, 1);
        }
    }

    private void startTVActivity() {
        Intent shim = new Intent(this, ForumsTVActivity.class);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            shim.putExtras(extras);
        }

        startActivity(shim);
        finish();
    }
    
    @Override
    public void displayThread(int id, int page, int fId, int fPg){
    	if(mViewPager != null){
    		mThreadId = id;
    		mThreadPage = page;
    		if(mThreadFragment != null){
    			mThreadFragment.openThread(id, page);
    		}
    		mViewPager.setCurrentItem(2);
    	}else{
    		super.displayForum(id, page);
    	}
    }
}

