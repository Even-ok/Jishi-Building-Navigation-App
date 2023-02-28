package com.example.supermap_demo.expandtabview;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.example.supermap_demo.R;

import java.util.ArrayList;

/**
 *@Description:菜单控件头部 封装了下拉动画 动态生成头部按钮个数
 *@Author:ben
 *@Since:2016年4月13日下午12:54:01
 */
public class SearchTabView extends LinearLayout implements OnDismissListener {

    private ToggleButton selectedButton;
    private ArrayList<String> mTextArray    = new ArrayList<String> ();
    private ArrayList<RelativeLayout> mViewArray    = new ArrayList<RelativeLayout> ();
    private ArrayList<ToggleButton>   mToggleButton = new ArrayList<ToggleButton> ();
    private Context                   mContext;
    private final int                 SMALL         = 0;
    private int                       displayWidth;
    private int                       displayHeight;
    private PopupWindow popupWindow;
    private int                       selectPosition;

    public SearchTabView(Context context) {
        super (context);
        init (context);
    }

    public SearchTabView(Context context, AttributeSet attrs) {
        super (context, attrs);
        init (context);
    }

    /**
     * 根据选择的位置设置tabitem显示的值
     */
    public void setTitle(String valueText,int position){
        if (position < mToggleButton.size ()) {
            mToggleButton.get (position).setText (valueText);
        }
    }

    /**
     * 根据选择的位置获取tabitem显示的值
     */
    public String getTitle(int position){
        if (position < mToggleButton.size () && mToggleButton.get (position).getText () != null) { return mToggleButton.get (position).getText ().toString (); }
        return "";
    }

    /**
     * 设置tabitem的个数和初始值
     */
    public void setValue(ArrayList<String> textArray,ArrayList<View> viewArray){
        if (mContext == null) { return; }
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);

        mTextArray = textArray;
        for ( int i = 0 ; i < viewArray.size () ; i++ ) {
            final RelativeLayout r = new RelativeLayout (mContext);
            int maxHeight = (int) (displayHeight * 0.7);
            RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams (RelativeLayout.LayoutParams.MATCH_PARENT,maxHeight);
            rl.leftMargin = 0;
            rl.rightMargin = 0;
            r.addView (viewArray.get (i), rl);
            mViewArray.add (r);
            r.setTag (SMALL);
            ToggleButton tButton = (ToggleButton) inflater.inflate (R.layout.btn_taggle_search, this, false);
            addView (tButton);

            // 分割线设置
            View line = new View  (mContext);
            line.setBackgroundColor (mContext.getResources ().getColor (R.color.text_hint));
            // line.setTop (10);
            // line.setBottom (10);
            LayoutParams lp;
            if (i < viewArray.size () - 1) {
                lp = new LayoutParams (1, LayoutParams.MATCH_PARENT);
                lp.setMargins (0, 10, 0, 10);
                addView (line, lp);

            }
            mToggleButton.add (tButton);
            tButton.setTag (i);
            tButton.setText (mTextArray.get (i));

            r.setOnClickListener (new OnClickListener () {

                @Override
                public void onClick(View v){
                    onPressBack ();
                }
            });

            r.setBackgroundColor (mContext.getResources ().getColor (R.color.popup_main_background));
            tButton.setOnClickListener (new OnClickListener () {

                @Override
                public void onClick(View view){
                    // initPopupWindow();
                    ToggleButton tButton = (ToggleButton) view;

                    if (selectedButton != null && selectedButton != tButton) {
                        selectedButton.setChecked (false);
                    }
                    selectedButton = tButton;
                    selectPosition = (Integer) selectedButton.getTag ();
                    startAnimation ();
                    if (mOnButtonClickListener != null && tButton.isChecked ()) {
                        mOnButtonClickListener.onClick (selectPosition);
                    }
                }
            });
        }
    }

    /**
     *@Description: 设置动画
     *@Author:胡帅
     *@Since: 2016年4月13日下午2:53:53
     */
    private void startAnimation(){

        if (popupWindow == null) {
            popupWindow = new PopupWindow (mViewArray.get (selectPosition),displayWidth,displayHeight);
            popupWindow.setAnimationStyle (R.style.PopupWindowAnimation);
            popupWindow.setFocusable (false);
            popupWindow.setOutsideTouchable (true);
        }

        if (selectedButton.isChecked ()) {
            if (!popupWindow.isShowing ()) {
                showPopup (selectPosition);
            } else {
                popupWindow.setOnDismissListener (this);
                popupWindow.dismiss ();
                hideView ();
            }
        } else {
            if (popupWindow.isShowing ()) {
                popupWindow.dismiss ();
                hideView ();
            }
        }
    }

    private void showPopup(int position){
        View tView = mViewArray.get (selectPosition).getChildAt (0);
        if (tView instanceof ViewBaseAction) {
            ViewBaseAction f = (ViewBaseAction) tView;
            f.show ();
        }
        if (popupWindow.getContentView () != mViewArray.get (position)) {
            popupWindow.setContentView (mViewArray.get (position));
        }
        popupWindow.showAsDropDown (this, 0, 0);
    }

    /**
     * 如果菜单成展开状态，则让菜单收回去
     */
    public boolean onPressBack(){
        if (popupWindow != null && popupWindow.isShowing ()) {
            popupWindow.dismiss ();
            hideView ();
            if (selectedButton != null) {
                selectedButton.setChecked (false);
            }
            return true;
        } else {
            return false;
        }

    }

    private void hideView(){
        View tView = mViewArray.get (selectPosition).getChildAt (0);
        if (tView instanceof ViewBaseAction) {
            ViewBaseAction f = (ViewBaseAction) tView;
            f.hide ();
        }
    }

    private void init(Context context){
        mContext = context;
        displayWidth = ((Activity) mContext).getWindowManager ().getDefaultDisplay ().getWidth ();
        displayHeight = ((Activity) mContext).getWindowManager ().getDefaultDisplay ().getHeight ();
        setOrientation (LinearLayout.HORIZONTAL);
    }

    @Override
    public void onDismiss(){
        showPopup (selectPosition);
        popupWindow.setOnDismissListener (null);
    }

    private OnButtonClickListener mOnButtonClickListener;

    /**
     * 设置tabitem的点击监听事件
     */
    public void setOnButtonClickListener(OnButtonClickListener l){
        mOnButtonClickListener = l;
    }

    /**
     * 自定义tabitem点击回调接口
     */
    public interface OnButtonClickListener {

        public void onClick(int selectPosition);
    }

}