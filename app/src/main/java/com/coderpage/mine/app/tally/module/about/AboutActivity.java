package com.coderpage.mine.app.tally.module.about;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.coderpage.mine.BuildConfig;
import com.coderpage.mine.R;
import com.coderpage.mine.app.tally.common.router.TallyRouter;
import com.coderpage.mine.ui.BaseActivity;

import java.util.Locale;

/**
 * @author abner-l. 2017-03-23
 */
@Route(path = TallyRouter.ABOUT)
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tally_about);

        TextView appVersionTv = (TextView) findViewById(R.id.tvAppVersion);
        String version = String.format(Locale.US, "%s (%d)",
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        appVersionTv.setText(version);

        findViewById(R.id.lyAppInfo).setOnLongClickListener(v -> {
            Toast.makeText(this, BuildConfig.FLAVOR, Toast.LENGTH_SHORT).show();
            return true;
        });
        findViewById(R.id.lyWeChatInfo).setOnClickListener(v -> {
            copyWeChatNumber();
            Toast.makeText(this, R.string.tally_about_wechat_copied, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setToolbarAsBack((View v) -> finish());
    }

    /** 复制原作者微信公众号 */
    public void copyWeChatNumber() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            return;
        }
        ClipData.Item clipItem = new ClipData.Item("miniapp");
        ClipData clipData = new ClipData("微信公众号", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, clipItem);
        clipboardManager.setPrimaryClip(clipData);
    }
}
