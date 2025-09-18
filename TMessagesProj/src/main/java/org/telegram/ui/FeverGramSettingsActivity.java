package org.telegram.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
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
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextCheckCell2;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.messenger.MessagesController;

import java.util.ArrayList;
import java.util.Locale;

public class FeverGramSettingsActivity extends BaseFragment {

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;

    private int adsSectionRow;
    private int disableAdsRow;
    private int adsSectionInfoRow;
    
    private int interfaceSectionRow;
    private int disableCountRoundingRow;
    private int showSecondsInTimeRow;
    private int customAppNameRow;
    private int centerTitleRow;
    private int interfaceSectionInfoRow;
    
    private int tosBreakingSectionRow;
    private int disableTypingPacketsRow;
    private int disableOnlinePacketsRow;
    private int disableReadPacketsRow;
    private int tosBreakingSectionInfoRow;
    
    private int debugSectionRow;
    private int logsRow;
    private int channelRow;
    private int debugSectionInfoRow;
    
     private int versionSectionRow;
     private int versionRow;
    private int rowsCount;
     
     private boolean ghostModeMenuExpanded = false;
     private static final Object payload = new Object();
 
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
        if (SharedConfig.centerTitle) {
            actionBar.updateTitleGravity();
        }
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
                SharedConfig.disableAds = !SharedConfig.disableAds;
                SharedConfig.saveConfig();
                ((TextCheckCell) view).setChecked(SharedConfig.disableAds);
                
                BulletinFactory.of(this).createSimpleBulletin(
                    R.drawable.msg_block,
                    SharedConfig.disableAds ? LocaleController.getString("AdsDisabled", R.string.AdsDisabled) : LocaleController.getString("AdsEnabled", R.string.AdsEnabled)
                ).show();
            } else if (position == disableCountRoundingRow) {
                SharedConfig.disableCountRounding = !SharedConfig.disableCountRounding;
                SharedConfig.saveConfig();
                ((TextCheckCell) view).setChecked(SharedConfig.disableCountRounding);
            } else if (position == showSecondsInTimeRow) {
                SharedConfig.showSecondsInTime = !SharedConfig.showSecondsInTime;
                SharedConfig.saveConfig();
                ((TextCheckCell) view).setChecked(SharedConfig.showSecondsInTime);
                LocaleController.getInstance().recreateFormatters();
            } else if (position == disableTypingPacketsRow) {
                 SharedConfig.disableTypingPackets = !SharedConfig.disableTypingPackets;
                 SharedConfig.saveConfig();
                 ((CheckBoxCell) view).setChecked(SharedConfig.disableTypingPackets, true);
                 
                 // Update main toggle
                 if (listAdapter != null) {
                     listAdapter.notifyItemChanged(tosBreakingSectionRow, payload);
                 }
                ConnectionsManager.native_setPacketsFilters(UserConfig.selectedAccount, SharedConfig.disableTypingPackets, SharedConfig.disableOnlinePackets);
             } else if (position == disableOnlinePacketsRow) {
                 SharedConfig.disableOnlinePackets = !SharedConfig.disableOnlinePackets;
                 SharedConfig.saveConfig();
                 ((CheckBoxCell) view).setChecked(SharedConfig.disableOnlinePackets, true);
                 
                 // Update main toggle
                 if (listAdapter != null) {
                     listAdapter.notifyItemChanged(tosBreakingSectionRow, payload);
                 }
                ConnectionsManager.native_setPacketsFilters(UserConfig.selectedAccount, SharedConfig.disableTypingPackets, SharedConfig.disableOnlinePackets);
                if (SharedConfig.disableOnlinePackets) {
                    MessagesController.getInstance(UserConfig.selectedAccount).ignoreSetOnline = true;
                    TL_account.updateStatus req = new TL_account.updateStatus();
                    req.offline = true;
                    ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> {});
                } else {
                    MessagesController.getInstance(UserConfig.selectedAccount).ignoreSetOnline = false;
                }
            } else if (position == disableReadPacketsRow) {
                SharedConfig.disableReadPackets = !SharedConfig.disableReadPackets;
                SharedConfig.saveConfig();
                ((CheckBoxCell) view).setChecked(SharedConfig.disableReadPackets, true);
                if (listAdapter != null) {
                    listAdapter.notifyItemChanged(tosBreakingSectionRow, payload);
                }
            } else if (position == logsRow) {
                presentFragment(new FeverGramLogsActivity());
            } else if (position == channelRow) {
                try {
                    Bundle args = new Bundle();
                    args.putLong("chat_id", 2964083732L);
                    if (!presentFragment(new ChatActivity(args))) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/c/2964083732/1"));
                        getParentActivity().startActivity(intent);
                    }
                } catch (Exception e) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/c/2964083732/1"));
                        getParentActivity().startActivity(intent);
                    } catch (Exception ex) {
                        
                    }
                }
            } else if (position == customAppNameRow) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.InterfaceTitle));
                final EditTextBoldCursor input = new EditTextBoldCursor(getParentActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                String currentName = MessagesController.getMainSettings(currentAccount).getString("FG_AppName", LocaleController.getString(R.string.AppName));
                input.setText(currentName);
                input.setHint(LocaleController.getString(R.string.AppName));
                builder.setView(input);
                builder.setPositiveButton(LocaleController.getString(R.string.OK), (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        newName = LocaleController.getString(R.string.AppName);
                    }
                    MessagesController.getMainSettings(currentAccount).edit().putString("FG_AppName", newName).apply();
                    if (listAdapter != null) listAdapter.notifyDataSetChanged();
                    if (getParentLayout() != null) {
                        getParentLayout().rebuildAllFragmentViews(false, false);
                    }
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                builder.setNeutralButton(LocaleController.getString(R.string.Default), (dialog, which) -> {
                    MessagesController.getMainSettings(currentAccount).edit().remove("FG_AppName").apply();
                    if (listAdapter != null) listAdapter.notifyDataSetChanged();
                    if (getParentLayout() != null) {
                        getParentLayout().rebuildAllFragmentViews(false, false);
                    }
                });
                showDialog(builder.create());
             } else if (position == centerTitleRow) {
                 SharedConfig.centerTitle = !SharedConfig.centerTitle;
                 SharedConfig.saveConfig();
                 if (listAdapter != null) listAdapter.notifyDataSetChanged();
                 if (getParentLayout() != null) {
                     getParentLayout().rebuildAllFragmentViews(false, false);
                 }
             } else if (position == tosBreakingSectionRow) {
                 ghostModeMenuExpanded = !ghostModeMenuExpanded;
                 updateRows();
                    if (listAdapter != null) {
                        listAdapter.notifyItemChanged(tosBreakingSectionRow, payload);
                        if (ghostModeMenuExpanded) {
                            listAdapter.notifyItemRangeInserted(tosBreakingSectionRow + 1, 3);
                        } else {
                            listAdapter.notifyItemRangeRemoved(tosBreakingSectionRow + 1, 3);
                        }
                    }
             }
        });
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }
    
    private int getGhostModeSelectedCount() {
        int count = 0;
        if (SharedConfig.disableTypingPackets) count++;
        if (SharedConfig.disableOnlinePackets) count++;
        if (SharedConfig.disableReadPackets) count++;
        return count;
    }
    
    private boolean isGhostModeActive() {
        return SharedConfig.disableTypingPackets && SharedConfig.disableOnlinePackets && SharedConfig.disableReadPackets;
    }

    private void updateRows() {
        int rowCount = 0;
        
        adsSectionRow = rowCount++;
        disableAdsRow = rowCount++;
        adsSectionInfoRow = rowCount++;
        
        interfaceSectionRow = rowCount++;
        disableCountRoundingRow = rowCount++;
        showSecondsInTimeRow = rowCount++;
        customAppNameRow = rowCount++;
        centerTitleRow = rowCount++;
        interfaceSectionInfoRow = rowCount++;
        
         tosBreakingSectionRow = rowCount++;
         if (ghostModeMenuExpanded) {
             disableTypingPacketsRow = rowCount++;
             disableOnlinePacketsRow = rowCount++;
             disableReadPacketsRow = rowCount++;
         } else {
             disableTypingPacketsRow = -1;
             disableOnlinePacketsRow = -1;
             disableReadPacketsRow = -1;
         }
         tosBreakingSectionInfoRow = rowCount++;
        
        debugSectionRow = rowCount++;
        logsRow = rowCount++;
        debugSectionInfoRow = rowCount++;
        
        versionSectionRow = rowCount++;
        versionRow = rowCount++;
        channelRow = rowCount++;
        
        rowsCount = rowCount;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
             return position == disableAdsRow || position == disableCountRoundingRow || position == showSecondsInTimeRow || 
                    position == disableTypingPacketsRow || position == disableOnlinePacketsRow || position == disableReadPacketsRow || position == logsRow || 
                    position == channelRow || position == customAppNameRow || position == centerTitleRow || position == tosBreakingSectionRow;
        }

        @Override
        public int getItemCount() {
            return rowsCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new HeaderCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 1:
                    view = new TextCheckCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    view = new TextSettingsCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new TextInfoPrivacyCell(context);
                    break;
                 case 5:
                     view = new ShadowSectionCell(context);
                     break;
                 case 6:
                     view = new TextCheckCell2(context);
                     view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                     break;
                 case 7:
                     view = new CheckBoxCell(context, 1);
                     view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                     break;
                 default:
                     view = new TextCell(context);
                     view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                     break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == adsSectionRow) {
                        headerCell.setText(LocaleController.getString("FeverGramTitle", R.string.FeverGramTitle));
                    } else if (position == interfaceSectionRow) {
                        headerCell.setText(LocaleController.getString("InterfaceTitle", R.string.InterfaceTitle));
                     } else if (position == debugSectionRow) {
                        headerCell.setText(LocaleController.getString("DebugTitle", R.string.DebugTitle));
                    } else if (position == versionSectionRow) {
                        headerCell.setText(LocaleController.getString("About", R.string.About));
                    }
                    break;
                case 1:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    if (position == disableAdsRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("DisableAds", R.string.DisableAds), SharedConfig.disableAds, true);
                    } else if (position == disableCountRoundingRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("DisableCountRounding", R.string.DisableCountRounding), SharedConfig.disableCountRounding, true);
                    } else if (position == showSecondsInTimeRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowSecondsInTime", R.string.ShowSecondsInTime), SharedConfig.showSecondsInTime, true);
                    } else if (position == disableTypingPacketsRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("DisableTypingPackets", R.string.DisableTypingPackets), SharedConfig.disableTypingPackets, true);
                    } else if (position == disableOnlinePacketsRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("DisableOnlinePackets", R.string.DisableOnlinePackets), SharedConfig.disableOnlinePackets, true);
                    } else if (position == centerTitleRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("CenterTitle", R.string.CenterTitle), SharedConfig.centerTitle, true);
                    }
                    break;
                case 2:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    if (position == customAppNameRow) {
                        String currentName = MessagesController.getMainSettings(currentAccount).getString("FG_AppName", null);
                        String value = (currentName != null && !currentName.isEmpty()) ? currentName : LocaleController.getString(R.string.Default);
                        textSettingsCell.setTextAndValue(LocaleController.getString(R.string.AppName), value, true);
                    }
                    break;
                case 3:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == logsRow) {
                        textCell.setText(LocaleController.getString("LogsTitle", R.string.LogsTitle), true);
                    } else if (position == channelRow) {
                        textCell.setText(LocaleController.getString("OfficialTgChannel", R.string.OfficialTgChannel), false);
                    } else if (position == versionRow) {
                        textCell.setTextAndValue(LocaleController.getString("Version", R.string.Version), "FeverGram " + BuildVars.GIT_COMMIT_HASH, false);
                    }
                    break;
                case 4:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == adsSectionInfoRow) {
                        textInfoPrivacyCell.setText(LocaleController.getString("AdsInfoText", R.string.AdsInfoText));
                    } else if (position == interfaceSectionInfoRow) {
                        textInfoPrivacyCell.setText(LocaleController.getString("InterfaceInfoText", R.string.InterfaceInfoText));
                    } else if (position == tosBreakingSectionInfoRow) {
                        textInfoPrivacyCell.setText(LocaleController.getString("TosBreakingInfoText", R.string.TosBreakingInfoText));
                     } else if (position == debugSectionInfoRow) {
                         textInfoPrivacyCell.setText(LocaleController.getString("DebugInfoText", R.string.DebugInfoText));
                     }
                     break;
                 case 6:
                     TextCheckCell2 textCheckCell2 = (TextCheckCell2) holder.itemView;
                     if (position == tosBreakingSectionRow) {
                         int selectedCount = getGhostModeSelectedCount();
                         textCheckCell2.setTextAndCheck(LocaleController.getString("TosBreakingTitle", R.string.TosBreakingTitle), isGhostModeActive(), true, true);
                        textCheckCell2.setCollapseArrow(String.format(Locale.US, "%d/3", selectedCount), !ghostModeMenuExpanded, () -> {
                            boolean newState = !isGhostModeActive();
                            SharedConfig.disableTypingPackets = newState;
                            SharedConfig.disableOnlinePackets = newState;
                            SharedConfig.disableReadPackets = newState;
                            SharedConfig.saveConfig();
                            ConnectionsManager.native_setPacketsFilters(UserConfig.selectedAccount, SharedConfig.disableTypingPackets, SharedConfig.disableOnlinePackets);
                            if (SharedConfig.disableOnlinePackets) {
                                MessagesController.getInstance(UserConfig.selectedAccount).ignoreSetOnline = true;
                                TL_account.updateStatus req = new TL_account.updateStatus();
                                req.offline = true;
                                ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> {});
                            } else {
                                MessagesController.getInstance(UserConfig.selectedAccount).ignoreSetOnline = false;
                            }
                            if (listAdapter != null) {
                                listAdapter.notifyItemChanged(tosBreakingSectionRow, payload);
                                if (ghostModeMenuExpanded) {
                                    listAdapter.notifyItemChanged(disableTypingPacketsRow);
                                    listAdapter.notifyItemChanged(disableOnlinePacketsRow);
                                    listAdapter.notifyItemChanged(disableReadPacketsRow);
                                }
                            }
                        });
                     }
                     break;
                 case 7:
                     CheckBoxCell checkBoxCell = (CheckBoxCell) holder.itemView;
                     if (position == disableTypingPacketsRow) {
                         checkBoxCell.setText(LocaleController.getString("DisableTypingPackets", R.string.DisableTypingPackets), "", SharedConfig.disableTypingPackets, true, true);
                    } else if (position == disableOnlinePacketsRow) {
                         checkBoxCell.setText(LocaleController.getString("DisableOnlinePackets", R.string.DisableOnlinePackets), "", SharedConfig.disableOnlinePackets, true, false);
                    } else if (position == disableReadPacketsRow) {
                        checkBoxCell.setText(LocaleController.getString("DisableReadPackets", R.string.DisableReadPackets), "", SharedConfig.disableReadPackets, true, false);
                     }
                     checkBoxCell.setPad(1);
                     break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == adsSectionRow || position == interfaceSectionRow || position == debugSectionRow || position == versionSectionRow) {
                return 0;
            } else if (position == disableAdsRow || position == disableCountRoundingRow || position == showSecondsInTimeRow || 
                       position == centerTitleRow) {
                return 1;
            } else if (position == customAppNameRow) {
                return 2;
            } else if (position == logsRow || position == channelRow || position == versionRow) {
                return 3;
            } else if (position == adsSectionInfoRow || position == interfaceSectionInfoRow || 
                       position == tosBreakingSectionInfoRow || position == debugSectionInfoRow) {
                return 4;
            } else if (position == tosBreakingSectionRow) {
                return 6;
            } else if (position == disableTypingPacketsRow || position == disableOnlinePacketsRow || position == disableReadPacketsRow) {
                return 7;
            }
            return 0;
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();
        
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{HeaderCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));
        
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
        
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));
        
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGray));
        
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));
        
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextCheckCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextSettingsCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));
        
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));
        
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));
        
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));
        
        return themeDescriptions;
    }
}
