package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.LayoutHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FeverGramLogsActivity extends BaseFragment {

    private ActionBarMenuItem searchItem;
    private ScrollView logScrollView;
    private TextView logView;
    private volatile boolean reading;
    private String currentFilter = "";
    private boolean paused = false;
    private boolean follow = true;
    
    private final Runnable updater = new Runnable() {
        @Override
        public void run() {
            if (fragmentView == null) return;
            readLogs();
            AndroidUtilities.runOnUIThread(this, 2000);
        }
    };
    private int lastTextLength;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("LogsTitle", R.string.LogsTitle));
        
        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(1, R.drawable.ic_ab_other).addSubItem(10, R.drawable.msg_delete, LocaleController.getString("DebugClearLogs", R.string.DebugClearLogs));
        menu.addItem(2, R.drawable.ic_ab_other).addSubItem(20, R.drawable.msg_mute, paused ? LocaleController.getString("ResumeLogs", R.string.ResumeLogs) : LocaleController.getString("PauseLogs", R.string.PauseLogs));
        menu.addItem(3, R.drawable.ic_ab_other).addSubItem(30, R.drawable.msg_discussion, follow ? LocaleController.getString("UnfollowLogs", R.string.UnfollowLogs) : LocaleController.getString("FollowLogs", R.string.FollowLogs));
        
        searchItem = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true)
            .setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
                @Override
                public void onSearchExpand() {
                    
                }
                
                @Override
                public void onSearchCollapse() {
                    currentFilter = "";
                    readLogs();
                }
                
                @Override
                public void onTextChanged(android.widget.EditText editText) {
                    currentFilter = editText.getText().toString();
                    readLogs();
                }
            });
        searchItem.setSearchFieldHint(LocaleController.getString("SearchLogsHint", R.string.SearchLogsHint));
        
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == 10) {
                    logView.setText("");
                } else if (id == 20) {
                    paused = !paused;
                } else if (id == 30) {
                    follow = !follow;
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        logScrollView = new ScrollView(context);
        logScrollView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        logScrollView.setFillViewport(true);
        
        logView = new TextView(context);
        logView.setTextIsSelectable(true);
        logView.setTextSize(11);
        logView.setTypeface(AndroidUtilities.getTypeface("fonts/rmono.ttf"));
        int pad = AndroidUtilities.dp(8);
        logView.setPadding(pad, pad, pad, pad);
        logView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        logView.setVerticalScrollBarEnabled(true);
        
        logScrollView.addView(logView, new ScrollView.LayoutParams(
            ScrollView.LayoutParams.MATCH_PARENT, 
            ScrollView.LayoutParams.WRAP_CONTENT
        ));
        
        frameLayout.addView(logScrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        FileLog.ensureInitied();
        readLogs();
        AndroidUtilities.runOnUIThread(updater, 2000);

        return fragmentView;
    }

    private void readLogs() {
        if (reading || paused) return;
        reading = true;
        AndroidUtilities.runOnUIThread(() -> {
            new Thread(() -> {
                try {
                    File dir = AndroidUtilities.getLogsDir();
                    if (dir == null) return;
                    File latest = null;
                    File[] files = dir.listFiles((d, name) -> name.endsWith(".txt") && !name.contains("_mtproto") && !name.contains("_net") && !name.contains("_tonlib"));
                    if (files != null && files.length > 0) {
                        for (File f : files) {
                            if (latest == null || f.lastModified() > latest.lastModified()) latest = f;
                        }
                    }
                    if (latest == null) return;

                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(latest)));
                    String line;
                    String filterLower = currentFilter == null ? "" : currentFilter.toLowerCase();
                    int lineCount = 0;
                    int maxLines = 1000; // Ограничиваем количество строк для производительности
                    
                    while ((line = br.readLine()) != null && lineCount < maxLines) {
                        if (filterLower.isEmpty() || line.toLowerCase().contains(filterLower)) {
                            sb.append(line).append('\n');
                            lineCount++;
                        }
                    }
                    br.close();
                    
                    final String text = sb.toString();
                    AndroidUtilities.runOnUIThread(() -> {
                        if (logView != null) {
                            boolean wasNearBottom = false;
                            try {
                                if (logScrollView != null) {
                                    int scrollY = logScrollView.getScrollY();
                                    int height = logScrollView.getHeight();
                                    int contentHeight = logView.getHeight();
                                    wasNearBottom = contentHeight > 0 && contentHeight - (scrollY + height) < AndroidUtilities.dp(100);
                                }
                            } catch (Exception ignore) { }
                            
                            logView.setText(text);
                            
                            if (follow && wasNearBottom) {
                                AndroidUtilities.runOnUIThread(this::scrollToBottom, 100);
                            }
                            lastTextLength = text.length();
                        }
                    });
                } catch (Exception e) {
                    FileLog.e(e);
                } finally {
                    reading = false;
                }
            }).start();
        });
    }

    private void scrollToBottom() {
        if (logScrollView != null) {
            logScrollView.post(() -> logScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidUtilities.cancelRunOnUIThread(updater);
        AndroidUtilities.runOnUIThread(updater, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        AndroidUtilities.cancelRunOnUIThread(updater);
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();
        
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));
        
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder));
        
        if (logView != null) {
            themeDescriptions.add(new ThemeDescription(logView, 0, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        }
        if (logScrollView != null) {
            themeDescriptions.add(new ThemeDescription(logScrollView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));
        }
        
        return themeDescriptions;
    }
}


