package org.telegram.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Collections;

public class FeverGramSettingsActivity extends BaseFragment {

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;

    private int headerRow;
    private int disableAdsRow;
    private int adsInfoRow;
    private int disableCountRoundingRow;
    private int showSecondsInTimeRow;
    private int interfaceHeaderRow;
    private int interfaceInfoRow;
    private int channelRow;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRows();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("FeverGramSettings", R.string.FeverGramSettings));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(listAdapter = new ListAdapter(context));
        listView.setOnItemClickListener((view, position) -> {
            if (position == disableAdsRow) {
                TextCheckCell cell = (TextCheckCell) view;
                cell.setChecked(!cell.isChecked());
                
                SharedConfig.disableAds = cell.isChecked();
                SharedConfig.saveConfig();
                
                AlertsCreator.showSimpleToast(FeverGramSettingsActivity.this, SharedConfig.disableAds ? "Ads disabled" : "Ads enabled");
            } else if (position == disableCountRoundingRow) {
                TextCheckCell cell = (TextCheckCell) view;
                cell.setChecked(!cell.isChecked());
                
                SharedConfig.disableCountRounding = cell.isChecked();
                SharedConfig.saveConfig();
            } else if (position == showSecondsInTimeRow) {
                TextCheckCell cell = (TextCheckCell) view;
                cell.setChecked(!cell.isChecked());
                
                SharedConfig.showSecondsInTime = cell.isChecked();
                SharedConfig.saveConfig();
                
                LocaleController.getInstance().recreateFormatters();
            } else if (position == channelRow) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/fevergramoffc"));
                    getParentActivity().startActivity(intent);
                } catch (Exception e) {
                    
                }
            }
        });
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    private void updateRows() {
        ArrayList<Integer> rowCounts = new ArrayList<>();
        headerRow = rowCounts.size();
        rowCounts.add(0);
        disableAdsRow = rowCounts.size();
        rowCounts.add(0);
        adsInfoRow = rowCounts.size();
        rowCounts.add(0);
        
        interfaceHeaderRow = rowCounts.size();
        rowCounts.add(0);
        disableCountRoundingRow = rowCounts.size();
        rowCounts.add(0);
        showSecondsInTimeRow = rowCounts.size();
        rowCounts.add(0);
        interfaceInfoRow = rowCounts.size();
        rowCounts.add(0);
        channelRow = rowCounts.size();
        rowCounts.add(0);
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == disableAdsRow || position == disableCountRoundingRow || position == showSecondsInTimeRow || position == channelRow;
        }

        @Override
        public int getItemCount() {
            return 8;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new HeaderCell(context);
                    break;
                case 1:
                    view = new TextCheckCell(context);
                    break;
                case 2:
                    view = new TextInfoPrivacyCell(context);
                    break;
                case 3:
                    view = new TextCell(context);
                    break;
                default:
                    view = new TextCell(context);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerRow) {
                        headerCell.setText("FeverGram");
                    } else if (position == interfaceHeaderRow) {
                        headerCell.setText("Interface");
                    }
                    break;
                case 1:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    if (position == disableAdsRow) {
                        textCheckCell.setTextAndCheck("Disable ads", SharedConfig.disableAds, false);
                    } else if (position == disableCountRoundingRow) {
                        textCheckCell.setTextAndCheck("Disable count rounding", SharedConfig.disableCountRounding, true);
                    } else if (position == showSecondsInTimeRow) {
                        textCheckCell.setTextAndCheck("Show seconds in time", SharedConfig.showSecondsInTime, false);
                    }
                    break;
                case 3:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == channelRow) {
                        textCell.setText("Our OFFICIAL tg channel", false);
                    }
                    break;
                case 2:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == adsInfoRow) {
                        textInfoPrivacyCell.setText("Disable ads in channels. Works for all users, not just Premium. (how long we've been waiting for this)");
                    } else if (position == interfaceInfoRow) {
                        textInfoPrivacyCell.setText("Interface settings for Telegram");
                    }
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == headerRow || position == interfaceHeaderRow) {
                return 0;
            } else if (position == disableAdsRow || position == disableCountRoundingRow || position == showSecondsInTimeRow) {
                return 1;
            } else if (position == adsInfoRow || position == interfaceInfoRow) {
                return 2;
            } else if (position == channelRow) {
                return 3;
            }
            return 0;
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextCell.class, TextCheckCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
        return themeDescriptions;
    }
}
