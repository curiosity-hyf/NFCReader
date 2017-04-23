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

package com.curiosity.nfcreader.ui;

import static android.provider.Settings.ACTION_SETTINGS;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.util.Log;

import com.curiosity.nfcreader.R;
import com.curiosity.nfcreader.ThisApplication;

/**
 * 主界面
 */

public final class MainPage {

    /**
     * 返回 主界面内容
     * @param activity
     * @return 主界面内容
     */
	public static CharSequence getContent(Activity activity) {

		final NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
		final int resid;

		if (nfc == null)
			resid = R.string.info_nfc_notsupport;
		else if (!nfc.isEnabled())
			resid = R.string.info_nfc_disabled;
		else
			resid = R.string.info_nfc_nocard;
		Log.d("mytest", "resid: " + resid);
		String tip = ThisApplication.getStringResource(resid);

		return new SpanFormatter(new Handler(activity)).toSpanned(tip);
	}

	private static final class Handler implements SpanFormatter.ActionHandler {
		private final Activity activity;

		Handler(Activity activity) {
			this.activity = activity;
		}

		@Override
		public void handleAction(CharSequence name) {
			startNfcSettingsActivity();
		}

		private void startNfcSettingsActivity() {
			try {
				activity.startActivityForResult(new Intent("android.settings.NFC_SETTINGS"), 0);
			} catch (Exception e) {
				activity.startActivityForResult(new Intent(ACTION_SETTINGS), 0);
			}
		}
	}

	private MainPage() {
	}
}
