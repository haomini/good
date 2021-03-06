package com.example.zhiyicx.justdodagger2.base;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.tsnackbar.widget.TSnackbar;
import com.example.zhiyicx.justdodagger2.R;
import com.example.zhiyicx.justdodagger2.base.i.IBasePresenter;
import com.example.zhiyicx.justdodagger2.base.i.IBaseView;
import com.example.zhiyicx.justdodagger2.utils.DeviceUtils;
import com.example.zhiyicx.justdodagger2.utils.StatusBarUtils;
import com.example.zhiyicx.justdodagger2.utils.UIUtils;
import com.example.zhiyicx.justdodagger2.widget.linear.SoftInputLinearLayout;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import skin.support.widget.SkinCompatToolbar;

import static com.example.zhiyicx.justdodagger2.base.config.ConstantConfig.JITTER_SPACING_TIME;

/**
 * @Describe
 * @Author zhouhao
 * @Date 2017/8/28
 * @Contact 605626708@qq.com
 */

public abstract class BaseFragment<P extends IBasePresenter> extends RxFragment implements IBaseView<P> {
    @LayoutRes
    private final static int DEFAULT_TOOLBAR = R.layout.default_toolbar;
    @ColorRes
    private static final int DEFAULT_TOOLBAR_BACKGROUD_COLOR = R.color.white;// 默认的toolbar背景色
    @ColorRes
    private static final int DEFAULT_DIVIDER_COLOR = R.color.general_for_line;// 默认的toolbar下方分割线颜色
    @DrawableRes
    private static final int DEFAULT_TOOLBAR_LEFT_IMG = R.mipmap.topbar_back;// 默认的toolbar左边的图片，一般是返回键

    private Unbinder mUnBinder;             // butterKnife解绑器
    protected View mDocerView;              // FragmentView
    protected RxPermissions mRxPermissions; // RxPermissions
    protected P mPresenter;                 // presenter
    protected SkinCompatToolbar mToolbar;   // 可定制toolbar
    private TextView mToolbarLeft,          // toolbar左边
            mToolbarRight,                  // toolbar右边
            mToolbarCenter;                 // toolbar中间
    private ImageView mIvRefresh;           // toolbar旋转等待
    private View mStatusPlaceholderView;    // 顶替状态栏的bar
    private boolean mIscUseSatusbar;        // 是否使用顶替状态栏/透明

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDocerView = getContentView(inflater);
        mUnBinder = ButterKnife.bind(this, mDocerView);
        getContentView(inflater);
        if (usePermissions()) {
            mRxPermissions = new RxPermissions(getActivity());
            mRxPermissions.setLogging(true);
        }
        initView();
        return mDocerView;
    }

    private View getContentView(LayoutInflater inflater) {
        LinearLayout rootLinear;
        if (isLastFragment()) {
            rootLinear = new SoftInputLinearLayout(getContext());
            rootLinear.setFitsSystemWindows(true);
        } else {
            rootLinear = new LinearLayout(getContext());
        }
        rootLinear.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootLinear.setOrientation(LinearLayout.VERTICAL);
        if (setUseStatusBar() && setUseStatusView()) { // 是否添加和状态栏等高的占位 View
            mStatusPlaceholderView = new View(getContext());
            mStatusPlaceholderView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DeviceUtils.getStatuBarHeight(getContext())));
            if (StatusBarUtils.intgetType(getActivity().getWindow()) == 0 && ContextCompat.getColor(getContext(), getToolBarBackground()) == Color.WHITE) {
                mStatusPlaceholderView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.themeColor));
            } else {
                mStatusPlaceholderView.setBackgroundColor(ContextCompat.getColor(getContext(), getToolBarBackground()));
            }
            rootLinear.addView(mStatusPlaceholderView);
        }
        if (useToolbar()) {
            mToolbar = (SkinCompatToolbar) inflater.inflate(getToolbarLayout(), null);
            initDefaultToolBar(mToolbar);
            rootLinear.addView(mToolbar);
        }
        if (setUseStatusBar()) {
            // 状态栏顶上去
            StatusBarUtils.transparencyBar(getActivity());
        } else {
            // 状态栏不顶上去
            StatusBarUtils.setStatusBarColor(getActivity(), getToolBarBackground());
        }
        if (useToolbarDivider()) {
            View divider = new View(getContext());
            divider.setBackgroundResource(DEFAULT_DIVIDER_COLOR);
            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.divider_len)));
            rootLinear.addView(divider);
        }
        setToolBarTextColor();
        // 是否设置状态栏文字图标灰色，对 小米、魅族、Android 6.0 及以上系统有效
        if (setStatusbarGrey()) {
            StatusBarUtils.statusBarLightMode(getActivity());
        }

        // 添加内容体
        View bodyView = inflater.inflate(getBodyLayout(), null);
        if (useOverScroll()) {
            OverScrollDecoratorHelper.setUpStaticOverScroll(bodyView,
                    OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        }

        rootLinear.addView(bodyView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return rootLinear;
    }

    /**
     * 状态栏是否可用
     *
     * @return 默认不可用
     */
    protected boolean setUseStatusBar() {
        mIscUseSatusbar = /*Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && */Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        return mIscUseSatusbar;
    }

    /**
     * 设置是否需要添加和状态栏等高的占位 view
     *
     * @return
     */
    protected boolean setUseStatusView() {
        return mIscUseSatusbar;
    }

    /**
     * 根据toolbar的背景设置它的文字颜色
     */
    protected void setToolBarTextColor() {
        // 如果toolbar背景是白色的，就将文字颜色设置成黑色
        if (useToolbar() && ContextCompat.getColor(getContext(), getToolBarBackground()) == Color.WHITE) {
            mToolbarCenter.setTextColor(ContextCompat.getColor(getContext(), R.color.important_for_content));
            mToolbarRight.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.selector_text_color));
            mToolbarLeft.setTextColor(ContextCompat.getColor(getContext(), R.color.important_for_content));
        }
    }

    /**
     * 初始化toolbar布局,如果进行了自定义toolbar布局，就应该重写该方法
     */
    protected void initDefaultToolBar(View toolBarContainer) {
        toolBarContainer.setBackgroundResource(getToolBarBackground());
        mToolbarLeft = (TextView) toolBarContainer.findViewById(R.id.tv_toolbar_left);
        mToolbarRight = (TextView) toolBarContainer.findViewById(R.id.tv_toolbar_right);
        mToolbarCenter = (TextView) toolBarContainer.findViewById(R.id.tv_toolbar_center);
        mIvRefresh = (ImageView) toolBarContainer.findViewById(R.id.iv_refresh);
        // 如果标题为空，就隐藏它
        mToolbarCenter.setVisibility(TextUtils.isEmpty(setCenterTitle()) ? View.GONE : View.VISIBLE);
        mToolbarCenter.setText(setCenterTitle());
        mToolbarLeft.setVisibility(TextUtils.isEmpty(setLeftTitle()) && setLeftImg() == 0 ? View.GONE : View.VISIBLE);
        mToolbarLeft.setText(setLeftTitle());
        mToolbarRight.setVisibility(TextUtils.isEmpty(setRightTitle()) && setRightImg() == 0 ? View.GONE : View.VISIBLE);
        mToolbarRight.setText(setRightTitle());

        setToolBarLeftImage(setLeftImg());
        setToolBarRightImage(setRightImg());
        RxView.clicks(mToolbarLeft)
                .throttleFirst(JITTER_SPACING_TIME, TimeUnit.SECONDS)   //两秒钟之内只取一个点击事件，防抖操作
                .compose(this.<Void>bindToLifecycle())
                .subscribe(aVoid -> setLeftClick());
        RxView.clicks(mToolbarRight)
                .throttleFirst(JITTER_SPACING_TIME, TimeUnit.SECONDS)   //两秒钟之内只取一个点击事件，防抖操作
                .compose(this.<Void>bindToLifecycle())
                .subscribe(aVoid -> setRightClick());
        RxView.clicks(mToolbarCenter)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(aVoid -> setCenterClick());
    }

    protected void setCenterClick() {

    }

    protected void setRightClick() {

    }

    protected void setLeftClick() {
        getActivity().finish();
    }

    @DrawableRes
    private int setRightImg() {
        return 0;
    }

    protected CharSequence setRightTitle() {
        return null;
    }

    @DrawableRes
    protected int setLeftImg() {
        return DEFAULT_TOOLBAR_LEFT_IMG;
    }

    protected CharSequence setLeftTitle() {
        return null;
    }

    protected CharSequence setCenterTitle() {
        return null;
    }

    /**
     * 状态栏默认为灰色
     * 支持小米、魅族以及 6.0 以上机型
     *
     * @return
     */
    protected boolean setStatusbarGrey() {
        return true;
    }

    /**
     * set ToolBar left image
     *
     * @param resImg image resourse
     */
    protected void setToolBarLeftImage(int resImg) {
        mToolbarLeft.setCompoundDrawables(UIUtils.getCompoundDrawables(getContext(), resImg), null, null, null);
    }

    /**
     * set ToolBar left image
     *
     * @param resImg image resourse
     */
    protected void setToolBarRightImage(int resImg) {
        mToolbarRight.setCompoundDrawables(null, null, UIUtils.getCompoundDrawables(getContext(), resImg), null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    @Override
    public void setPresenter(P presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void showSnackErrorMessage(CharSequence s) {
        showSnackMessage(s, TSnackbar.ERROR);
    }

    @Override
    public void showSnackSuccessMessage(CharSequence s) {
        showSnackMessage(s, TSnackbar.SUCCESS);
    }

    @Override
    public void showSnackLoadingMessage(CharSequence s) {
        showSnackMessage(s, TSnackbar.LOADING);
    }

    protected void showSnackMessage(CharSequence s, @TSnackbar.Prompt int prompt) {
        TSnackbar.make(getView(), s, TSnackbar.LENGTH_SHORT, TSnackbar.APPEAR_FROM_TOP)
                .setPrompt(prompt)
                .setCallback(new TSnackbar.Callback() {
                    @Override
                    public void onDismissed(TSnackbar TSnackbar, @DismissEvent int event) {
                        showWhenSnackDismissed();
                    }
                })
                .show();
    }

    protected abstract void initView();

    protected abstract void initData();

    @LayoutRes
    protected abstract int getBodyLayout();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }

    @LayoutRes
    protected int getToolbarLayout() {
        return DEFAULT_TOOLBAR;
    }

    protected int getToolBarBackground() {
        return DEFAULT_TOOLBAR_BACKGROUD_COLOR;
    }

    protected boolean useToolbar() {
        return true;
    }

    protected boolean useToolbarDivider() {
        return true;
    }

    protected boolean usePermissions() {
        return false;
    }

    protected boolean useOverScroll() {
        return true;
    }


    protected void showWhenSnackDismissed() {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    // 使得仅存在一个SoftInputLinearLayout
    protected abstract boolean isLastFragment();
}
