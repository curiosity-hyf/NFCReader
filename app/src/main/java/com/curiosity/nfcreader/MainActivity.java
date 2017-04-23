/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.curiosity.nfcreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.curiosity.nfcreader.nfc.NfcManager;
import com.curiosity.nfcreader.ui.AboutPage;
import com.curiosity.nfcreader.ui.MainPage;
import com.curiosity.nfcreader.ui.NfcPage;
import com.curiosity.nfcreader.ui.Toolbar;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();

		nfc = new NfcManager(this);

		onNewIntent(getIntent());
	}

    /**
     * 按下后退键的回调
     */
	@Override
	public void onBackPressed() {
		if (isCurrentPage(SPEC.PAGE.ABOUT)) // 如果当前为 about 界面，则返回主界面
			loadDefaultPage();
		else if (safeExit)
			super.onBackPressed();
	}

	@Override
	public void setIntent(Intent intent) {
		if (NfcPage.isSendByMe(intent))
			loadNfcPage(intent);
		else if (AboutPage.isSendByMe(intent))
			loadAboutPage();
		else
			super.setIntent(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		nfc.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		nfc.onResume();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			if (nfc.updateStatus())
				loadDefaultPage();

			// 有些ROM将关闭系统状态下拉面板的BACK事件发给最顶层窗口
			// 这里加入一个延迟避免意外退出
			board.postDelayed(new Runnable() {
				public void run() {
					safeExit = true;
				}
			}, 800);
		} else {
			safeExit = false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		loadDefaultPage();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d("mytest", "onNewIntent " + intent.toString());
		if (!nfc.readCard(intent, new NfcPage(this))) {
            Log.d("mytest", "onNewIntent " + "loadDefaultPage");
            loadDefaultPage();
        } else {
            Log.d("mytest", "onNewIntent " + "no");
        }
	}

    /**
     * 切换至 主界面
     * @param view
     */
	public void onSwitch2DefaultPage(View view) {
		if (!isCurrentPage(SPEC.PAGE.DEFAULT))
			loadDefaultPage();
	}

    /**
     * 切换至 about 界面
     * @param view
     */
	public void onSwitch2AboutPage(View view) {
		if (!isCurrentPage(SPEC.PAGE.ABOUT))
			loadAboutPage();
	}

    /**
     * 复制当前界面的文本信息
     * @param view
     */
	public void onCopyPageContent(View view) {
		toolbar.copyPageContent(getCurrentPage());
	}

    /**
     * 分享当前界面的文本信息
     * @param view
     */
	public void onSharePageContent(View view) {
		toolbar.sharePageContent(getCurrentPage());
	}

    /**
     * 加载 主界面
     */
	private void loadDefaultPage() {
		toolbar.show(null);

		TextView ta = getNextPage(); // 获取下一界面的文本框

		resetTextArea(ta, SPEC.PAGE.DEFAULT, Gravity.CENTER);
		ta.setText(MainPage.getContent(this));

		board.showNext(); // 切换到下一个界面
	}

    /**
     * 加载 about 界面
     */
	private void loadAboutPage() {
		toolbar.show(R.id.btnBack);

		TextView ta = getNextPage(); // 获取下一界面的文本框

		resetTextArea(ta, SPEC.PAGE.ABOUT, Gravity.START);
		ta.setText(AboutPage.getContent(this)); // 设置内容

		board.showNext(); // 切换到下一个界面
	}

    /**
     * 加载 nfc 信息界面
     * @param intent
     */
	private void loadNfcPage(Intent intent) {
		final CharSequence info = NfcPage.getContent(this, intent);

		TextView ta = getNextPage(); // 获取下一界面的文本框

		if (NfcPage.isNormalInfo(intent)) {
			toolbar.show(R.id.btnCopy, R.id.btnShare, R.id.btnReset);
			resetTextArea(ta, SPEC.PAGE.INFO, Gravity.LEFT);
		} else {
			toolbar.show(R.id.btnBack);
			resetTextArea(ta, SPEC.PAGE.INFO, Gravity.CENTER);
		}

		ta.setText(info);

		board.showNext(); // 切换到下一个界面
	}

    /**
     * 判断 当前界面是否为指定界面
     * @param which 指定的界面
     * @return true or false
     */
	private boolean isCurrentPage(SPEC.PAGE which) {
		Object obj = getCurrentPage().getTag();

		if (obj == null)
			return which.equals(SPEC.PAGE.DEFAULT);

		return which.equals(obj);
	}

    /**
     * 预设 TextView 状态
     * @param textArea 指定的 TextView
     * @param type 界面类型
     * @param gravity gravity
     */
	private void resetTextArea(TextView textArea, SPEC.PAGE type, int gravity) {

		((View) textArea.getParent()).scrollTo(0, 0);

		textArea.setTag(type);
		textArea.setGravity(gravity);
	}

    /**
     * 获取当前界面的 TextView
     * @return TextView
     */
	private TextView getCurrentPage() {
		return (TextView) ((ViewGroup) board.getCurrentView()).getChildAt(0);
	}

    /**
     * 获取下一界面的 TextView
     * @return TextView
     */
	private TextView getNextPage() {
		return (TextView) ((ViewGroup) board.getNextView()).getChildAt(0);
	}

    /**
     * 初始化界面
     */
	private void initViews() {
		board = (ViewSwitcher) findViewById(R.id.switcher); // 视图切换器

//		Typeface tf = ThisApplication.getFontResource(R.string.font_oem1);
//		Log.d("mytest", "!!!!!!!!!!" + (tf==null));
		TextView tv = (TextView) findViewById(R.id.txtAppName);
//		tv.setTypeface(tf);

//		tf = ThisApplication.getFontResource(R.string.font_oem2);

		tv = getCurrentPage();
		tv.setMovementMethod(LinkMovementMethod.getInstance());
//		tv.setTypeface(tf);

		tv = getNextPage();
		tv.setMovementMethod(LinkMovementMethod.getInstance());
//		tv.setTypeface(tf);

		toolbar = new Toolbar((ViewGroup) findViewById(R.id.toolbar));
	}

	private ViewSwitcher board;
	private Toolbar toolbar;
	private NfcManager nfc;
	private boolean safeExit;
}
