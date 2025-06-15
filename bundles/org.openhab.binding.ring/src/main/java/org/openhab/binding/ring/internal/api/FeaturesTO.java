/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.ring.internal.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FeaturesTO} class is part of the profile description
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unused")
public class FeaturesTO {

    @SerializedName("mdlr_flc2_new")
    public boolean mdlrFlc2New;

    @SerializedName("aa_fr_yu_lt_enabled")
    public boolean aaFrYuLtEnabled;

    @SerializedName("high_contrast_mode")
    public boolean highContrastMode;

    @SerializedName("in_app_chat_enabled")
    public boolean inAppChatEnabled;

    @SerializedName("we_si_4_io_device_types")
    public List<String> weSi4IoDeviceTypes = List.of();

    @SerializedName("cs_et_mn_enabled")
    public boolean csEtMnEnabled;

    @SerializedName("live_quality_indicators_enabled")
    public boolean liveQualityIndicatorsEnabled;

    @SerializedName("video_feedback_enabled")
    public boolean videoFeedbackEnabled;

    @SerializedName("gn_tt_enabled")
    public boolean gnTtEnabled;

    @SerializedName("ll_lk_cp_gw_device_types")
    public List<String> llLkCpGwDeviceTypes = List.of();

    @SerializedName("am_hb_rg_rr_ad_enabled")
    public boolean amHbRgRrAdEnabled;

    @SerializedName("invite_program_without_credit")
    public boolean inviteProgramWithoutCredit;

    @SerializedName("pt_al_rg_enabled")
    public boolean ptAlRgEnabled;

    @SerializedName("ay_ul_1_enabled")
    public boolean ayUl1Enabled;

    @SerializedName("pt_vs_st_enabled")
    public boolean ptVsStEnabled;

    @SerializedName("dashboard_ms_p1")
    public boolean dashboardMsP1;

    @SerializedName("ee_he_sd_ur_2_enabled")
    public boolean eeHeSdUr2Enabled;

    @SerializedName("nw_notification_radius_enabled")
    public boolean nwNotificationRadiusEnabled;

    @SerializedName("mo_ve_device_types")
    public List<String> moVeDeviceTypes = List.of();

    @SerializedName("alarm_700_series_enabled")
    public boolean alarm700SeriesEnabled;

    @SerializedName("et_hy_cr_enabled")
    public boolean etHyCrEnabled;

    @SerializedName("group_sharing_v2_enabled")
    public boolean groupSharingV2Enabled;

    @SerializedName("ad_ge_ly_it_enabled")
    public boolean adGeLyItEnabled;

    @SerializedName("sa_ln_st_me_enabled")
    public boolean saLnStMeEnabled;

    @SerializedName("cocoa_dl_pwr_mds_enabled_v3")
    public boolean cocoaDlPwrMdsEnabledV3;

    @SerializedName("rn_pn_ip_lp_enabled")
    public boolean rnPnIpLpEnabled;

    @SerializedName("dc_mw_enabled")
    public boolean dcMwEnabled;

    @SerializedName("ring_alarm_enabled")
    public boolean ringAlarmEnabled;

    @SerializedName("cl_as_ff4_enabled")
    public boolean clAsFf4Enabled;

    @SerializedName("cv_tr_dl_st_enabled")
    public boolean cvTrDlStEnabled;

    @SerializedName("nw_v2_enabled")
    public boolean nwV2Enabled;

    @SerializedName("alarm_first_alert_500_enabled")
    public boolean alarmFirstAlert500Enabled;

    @SerializedName("test-bug-bash-gradual-rollout-ehsad-boolean")
    public boolean testBugBashGradualRolloutEhsadBoolean;

    @SerializedName("im_ht_ao_enabled")
    public boolean imHtAoEnabled;

    @SerializedName("ps_an_enabled")
    public boolean psAnEnabled;

    @SerializedName("device_controls_menu_enabled")
    public boolean deviceControlsMenuEnabled;

    @SerializedName("cocoa_pwr_mds_v2_enabled")
    public boolean cocoaPwrMdsV2Enabled;

    @SerializedName("aed_enabled")
    public boolean aedEnabled;

    @SerializedName("p_br_oa_enabled")
    public boolean pBrOaEnabled;

    @SerializedName("default_snapshot_tile_dashboard_enable")
    public boolean defaultSnapshotTileDashboardEnable;

    @SerializedName("scrubber_auto_live_enabled")
    public boolean scrubberAutoLiveEnabled;

    @SerializedName("2v_pe_3_nt_ot_x_enabled")
    public boolean jsonMember2vPe3NtOtXEnabled;

    @SerializedName("snapshot_tile_dashboard_enabled")
    public boolean snapshotTileDashboardEnabled;

    @SerializedName("ky_vr_at_enabled")
    public boolean kyVrAtEnabled;

    @SerializedName("te_le_1_enabled")
    public boolean teLe1Enabled;

    @SerializedName("pp_am_ee_enabled")
    public boolean ppAmEeEnabled;

    @SerializedName("lv_or_enabled")
    public boolean lvOrEnabled;

    @SerializedName("bomgar_enabled")
    public boolean bomgarEnabled;

    @SerializedName("gl_br_enabled")
    public boolean glBrEnabled;

    @SerializedName("ul_ss_device_types")
    public List<String> ulSsDeviceTypes = List.of();

    @SerializedName("motion_zones_v2_enabled")
    public boolean motionZonesV2Enabled;

    @SerializedName("mx_on_sk_enabled")
    public boolean mxOnSkEnabled;

    @SerializedName("re_bd_as_cl_enabled")
    public boolean reBdAsClEnabled;

    @SerializedName("ca_fl_cm_enabled")
    public boolean caFlCmEnabled;

    @SerializedName("rs.devicecatalog.whitelist.device.yale700")
    public boolean rsDevicecatalogWhitelistDeviceYale700;

    @SerializedName("ns_v2_1_enabled")
    public boolean nsV21Enabled;

    @SerializedName("dl_pr_ms_v1_1_enabled")
    public boolean dlPrMsV11Enabled;

    @SerializedName("mx_st_ml_rd_enabled")
    public boolean mxStMlRdEnabled;

    @SerializedName("st_as_device_types")
    public List<String> stAsDeviceTypes = List.of();

    @SerializedName("mw_me_enabled")
    public boolean mwMeEnabled;

    @SerializedName("name_peephole_toggle_enabled")
    public boolean namePeepholeToggleEnabled;

    @SerializedName("mdlr_flc_35")
    public boolean mdlrFlc35;

    @SerializedName("pt_lp_wvsrt_enabled")
    public boolean ptLpWvsrtEnabled;

    @SerializedName("cfes_enabled")
    public boolean cfesEnabled;

    @SerializedName("ko_ty_ln_enabled")
    public boolean koTyLnEnabled;

    @SerializedName("sh_ti_enabled")
    public boolean shTiEnabled;

    @SerializedName("device_settings_v2_enabled")
    public boolean deviceSettingsV2Enabled;

    @SerializedName("an_eo_ta_ds_enabled")
    public boolean anEoTaDsEnabled;

    @SerializedName("wb_vw_v2_enabled")
    public boolean wbVwV2Enabled;

    @SerializedName("video_jitter_buffer_ms")
    public int videoJitterBufferMs;

    @SerializedName("sw_l_sh_enabled")
    public boolean swLShEnabled;

    @SerializedName("ld_v4_enable")
    public boolean ldV4Enable;

    @SerializedName("qk_cs_1_le_enabled")
    public boolean qkCs1LeEnabled;

    @SerializedName("sw_zn_rcmdtns")
    public boolean swZnRcmdtns;

    @SerializedName("pt_lp_sc_enabled")
    public boolean ptLpScEnabled;

    @SerializedName("landscape_view_device_controls_menu_enabled")
    public boolean landscapeViewDeviceControlsMenuEnabled;

    @SerializedName("bs_lt_mr_sp_enabled")
    public boolean bsLtMrSpEnabled;

    @SerializedName("ns_v2_1_ff2_enabled")
    public boolean nsV21Ff2Enabled;

    @SerializedName("pp_sd_db_enabled")
    public boolean ppSdDbEnabled;

    @SerializedName("ge_gd_ff2_enabled")
    public boolean geGdFf2Enabled;

    @SerializedName("rp_re_enabled")
    public boolean rpReEnabled;

    @SerializedName("wc_pk_cs_tni_enabled")
    public boolean wcPkCsTniEnabled;

    @SerializedName("app_alert_tones_enabled")
    public boolean appAlertTonesEnabled;

    @SerializedName("video_search_enabled")
    public boolean videoSearchEnabled;

    @SerializedName("zs_mp_enabled")
    public boolean zsMpEnabled;

    @SerializedName("wc_pk_cs_f2_enabled")
    public boolean wcPkCsF2Enabled;

    @SerializedName("bz_longfin_enabled")
    public boolean bzLongfinEnabled;

    @SerializedName("scallop_lite_v3_enabled")
    public boolean scallopLiteV3Enabled;

    @SerializedName("legacy_cvr_retention_enabled")
    public boolean legacyCvrRetentionEnabled;

    @SerializedName("rl_re_fm_gp_enabled")
    public boolean rlReFmGpEnabled;

    @SerializedName("avp_1_3_enabled")
    public boolean avp13Enabled;

    @SerializedName("smart_bulbs_device_types_enabled")
    public List<String> smartBulbsDeviceTypesEnabled = List.of();

    @SerializedName("gt_as_ky_enabled")
    public boolean gtAsKyEnabled;

    @SerializedName("wr_an_ba_ms")
    public boolean wrAnBaMs;

    @SerializedName("amazon_account_linking_enabled")
    public boolean amazonAccountLinkingEnabled;

    @SerializedName("sk_up_cm_ee_enabled")
    public boolean skUpCmEeEnabled;

    @SerializedName("mdlr_flc_nov")
    public boolean mdlrFlcNov;

    @SerializedName("ce_yr_bs_enabled")
    public boolean ceYrBsEnabled;

    @SerializedName("qr_ce_sr_v2_enabled")
    public boolean qrCeSrV2Enabled;

    @SerializedName("fw_wm_c1_enabled")
    public boolean fwWmC1Enabled;

    @SerializedName("bs_be_mr_sp_enabled")
    public boolean bsBeMrSpEnabled;

    @SerializedName("ring_beams_enabled")
    public boolean ringBeamsEnabled;

    @SerializedName("pt_qrc_enabled")
    public boolean ptQrcEnabled;

    @SerializedName("aa_st_te_fr_yu_enabled")
    public boolean aaStTeFrYuEnabled;

    @SerializedName("sr_by_sc_enabled")
    public boolean srByScEnabled;

    @SerializedName("vrl31_lpd_enabled")
    public boolean vrl31LpdEnabled;

    @SerializedName("rw_pt_enabled")
    public boolean rwPtEnabled;

    @SerializedName("ale_grd_enabled")
    public boolean aleGrdEnabled;

    @SerializedName("e2_su_ca_fr_enabled")
    public boolean e2SuCaFrEnabled;

    @SerializedName("rn_jn_enabled")
    public boolean rnJnEnabled;

    @SerializedName("alarm_norway_enabled")
    public boolean alarmNorwayEnabled;

    @SerializedName("mdlr_g_aff")
    public boolean mdlrGAff;

    @SerializedName("ky_2_rg_pk_pl_enabled")
    public boolean ky2RgPkPlEnabled;

    @SerializedName("hw_ts_enabled")
    public boolean hwTsEnabled;

    @SerializedName("manual_exposure_enabled")
    public boolean manualExposureEnabled;

    @SerializedName("rs.devicecatalog.whitelist.device.yale500")
    public boolean rsDevicecatalogWhitelistDeviceYale500;

    @SerializedName("ha_uc_bs_we_rb_enabled")
    public boolean haUcBsWeRbEnabled;

    @SerializedName("rg_im_enabled")
    public boolean rgImEnabled;

    @SerializedName("sp_cm_ps_enabled")
    public boolean spCmPsEnabled;

    @SerializedName("jx_be_st_enabled")
    public boolean jxBeStEnabled;

    @SerializedName("ld_zs_st_enabled")
    public boolean ldZsStEnabled;

    @SerializedName("se_cs_enabled")
    public boolean seCsEnabled;

    @SerializedName("ios_native_websockets_enabled")
    public boolean iosNativeWebsocketsEnabled;

    @SerializedName("vl_cx_ff2_enabled")
    public boolean vlCxFf2Enabled;

    @SerializedName("pt_lp_rsb_enabled")
    public boolean ptLpRsbEnabled;

    @SerializedName("linked_devices_tile_enabled")
    public boolean linkedDevicesTileEnabled;

    @SerializedName("ee_he_sd_ur_enabled")
    public boolean eeHeSdUrEnabled;

    @SerializedName("pt_vs_gr_enabled")
    public boolean ptVsGrEnabled;

    @SerializedName("sf_se_cy_enabled")
    public boolean sfSeCyEnabled;

    @SerializedName("py_cd_oe_enabled")
    public boolean pyCdOeEnabled;

    @SerializedName("to_vn_ss_enabled")
    public boolean toVnSsEnabled;

    @SerializedName("py_ne_enabled")
    public boolean pyNeEnabled;

    @SerializedName("video_resolutions_dpd_enabled")
    public boolean videoResolutionsDpdEnabled;

    @SerializedName("hy_sg_pl_enabled")
    public boolean hySgPlEnabled;

    @SerializedName("rp_enabled")
    public boolean rpEnabled;

    @SerializedName("rh_tn_nt_ln_enabled")
    public boolean rhTnNtLnEnabled;

    @SerializedName("cm_pt_pr_enabled")
    public boolean cmPtPrEnabled;

    @SerializedName("le_pv_enabled")
    public boolean lePvEnabled;

    @SerializedName("cl_cr_op_enabled")
    public boolean clCrOpEnabled;

    @SerializedName("device_traits_enabled")
    public boolean deviceTraitsEnabled;

    @SerializedName("gelato_v3_enabled")
    public boolean gelatoV3Enabled;

    @SerializedName("RCSanityTest1")
    public boolean rCSanityTest1;

    @SerializedName("cn_mg_me_2_enabled")
    public boolean cnMgMe2Enabled;

    @SerializedName("ss_ts_fr_am_enabled")
    public boolean ssTsFrAmEnabled;

    @SerializedName("pom_recording_preferences_enabled")
    public boolean pomRecordingPreferencesEnabled;

    @SerializedName("alarm_do_not_disturb_enabled")
    public boolean alarmDoNotDisturbEnabled;

    @SerializedName("it_bd_pg_fw_number")
    public int itBdPgFwNumber;

    @SerializedName("ld_ss_ce_po_enabled")
    public boolean ldSsCePoEnabled;

    @SerializedName("rg_hp_rn_ff2_enabled")
    public boolean rgHpRnFf2Enabled;

    @SerializedName("bm_st_ce_sr_enabled")
    public boolean bmStCeSrEnabled;

    @SerializedName("he_pe_lk_sp_enabled")
    public boolean hePeLkSpEnabled;

    @SerializedName("rn_opt_in_enabled")
    public boolean rnOptInEnabled;

    @SerializedName("qk_rs_fr_ee_enabled")
    public boolean qkRsFrEeEnabled;

    @SerializedName("live_view_settings_enabled")
    public boolean liveViewSettingsEnabled;

    @SerializedName("mdlr_drb_2_35")
    public boolean mdlrDrb235;

    @SerializedName("rh_tn_nt_ln_ff2_enabled")
    public boolean rhTnNtLnFf2Enabled;

    @SerializedName("scallop_pre_roll_enabled")
    public boolean scallopPreRollEnabled;

    @SerializedName("ps_an_ff3_enabled")
    public boolean psAnFf3Enabled;

    @SerializedName("nw_enabled")
    public boolean nwEnabled;

    @SerializedName("scallop_v2_enabled")
    public boolean scallopV2Enabled;

    @SerializedName("ft_cm_po_enabled")
    public boolean ftCmPoEnabled;

    @SerializedName("delete_all_settings_enabled")
    public boolean deleteAllSettingsEnabled;

    @SerializedName("nw_enc_flw_ff2_enabled")
    public boolean nwEncFlwFf2Enabled;

    @SerializedName("rpp_education_screens_enabled")
    public boolean rppEducationScreensEnabled;

    @SerializedName("mdlr_elt")
    public boolean mdlrElt;

    @SerializedName("vl_s2u_enabled")
    public boolean vlS2uEnabled;

    @SerializedName("ao_sy_device_types")
    public List<String> aoSyDeviceTypes = List.of();

    @SerializedName("ma_be_sp_enabled")
    public boolean maBeSpEnabled;

    @SerializedName("advanced_motion_detection_device_types")
    public List<String> advancedMotionDetectionDeviceTypes = List.of();

    @SerializedName("mdlr_sclp_35")
    public boolean mdlrSclp35;

    @SerializedName("mdlr_chm")
    public boolean mdlrChm;

    @SerializedName("it_bd_pg_fw_enabled")
    public boolean itBdPgFwEnabled;

    @SerializedName("sw_oc_lg_v2_enabled")
    public boolean swOcLgV2Enabled;

    @SerializedName("amazon_key_tile_unlock")
    public boolean amazonKeyTileUnlock;

    @SerializedName("ff_24x7_lite_frequency_switch_enabled")
    public boolean ff24x7LiteFrequencySwitchEnabled;

    @SerializedName("pt_lp_we_enabled")
    public boolean ptLpWeEnabled;

    @SerializedName("night_mode_lunar")
    public boolean nightModeLunar;

    @SerializedName("non_dispatch_monitoring_enabled")
    public boolean nonDispatchMonitoringEnabled;

    @SerializedName("eh_te_nve_enabled")
    public boolean ehTeNveEnabled;

    @SerializedName("mn_wg_ld_enabled")
    public boolean mnWgLdEnabled;

    @SerializedName("oa_st_enabled")
    public boolean oaStEnabled;

    @SerializedName("wk_wb_vw_enabled")
    public boolean wkWbVwEnabled;

    @SerializedName("bar_enabled")
    public boolean barEnabled;

    @SerializedName("ed_am_se_br_enabled")
    public boolean edAmSeBrEnabled;

    @SerializedName("mp_enabled")
    public boolean mpEnabled;

    @SerializedName("mdlr_aff")
    public boolean mdlrAff;

    @SerializedName("cl_cd_mn_dd_enabled")
    public boolean clCdMnDdEnabled;

    @SerializedName("knock_alerts_enabled")
    public boolean knockAlertsEnabled;

    @SerializedName("go_dy_ky_ms_enabled")
    public boolean goDyKyMsEnabled;

    @SerializedName("alarm_ireland_enabled")
    public boolean alarmIrelandEnabled;

    @SerializedName("od_gd_ff2_enabled")
    public boolean odGdFf2Enabled;

    @SerializedName("tline_gapless_full_enabled")
    public boolean tlineGaplessFullEnabled;

    @SerializedName("battery_hw_h264_decoder_enabled")
    public boolean batteryHwH264DecoderEnabled;

    @SerializedName("vl_cx_as_cs_enabled")
    public boolean vlCxAsCsEnabled;

    @SerializedName("cs_ap_ct_enabled")
    public boolean csApCtEnabled;

    @SerializedName("cn_me_device_types")
    public List<String> cnMeDeviceTypes = List.of();

    @SerializedName("sw_oc_srn_enabled")
    public boolean swOcSrnEnabled;

    @SerializedName("alarm_web_700_sensitivity_enabled")
    public boolean alarmWeb700SensitivityEnabled;

    @SerializedName("vl_pd_rr_as_enabled")
    public boolean vlPdRrAsEnabled;

    @SerializedName("advanced_motion_zones_device_types")
    public List<Object> advancedMotionZonesDeviceTypes = List.of();

    @SerializedName("sw_sw_cs_device_types")
    public List<String> swSwCsDeviceTypes = List.of();

    @SerializedName("advanced_motion_detection_human_only_mode_enabled")
    public boolean advancedMotionDetectionHumanOnlyModeEnabled;

    @SerializedName("streaming_data_in_pn_enabled")
    public boolean streamingDataInPnEnabled;

    @SerializedName("me_se_ba_enabled")
    public boolean meSeBaEnabled;

    @SerializedName("le_sk_enabled")
    public boolean leSkEnabled;

    @SerializedName("pp_sd_md_enabled")
    public boolean ppSdMdEnabled;

    @SerializedName("group_sharing_enabled")
    public boolean groupSharingEnabled;

    @SerializedName("be_sp_device_types")
    public List<String> beSpDeviceTypes = List.of();

    @SerializedName("ce_as_12_enabled")
    public boolean ceAs12Enabled;

    @SerializedName("ce_po_bd_cd_enabled")
    public boolean cePoBdCdEnabled;

    @SerializedName("bd_ee_sb_in_enabled")
    public boolean bdEeSbInEnabled;

    @SerializedName("alarm_denmark_enabled")
    public boolean alarmDenmarkEnabled;

    @SerializedName("slr_smt_lts_brtss_lvl_enabled")
    public boolean slrSmtLtsBrtssLvlEnabled;

    @SerializedName("people_only_mode_lpd_enabled")
    public boolean peopleOnlyModeLpdEnabled;

    @SerializedName("p_s_oa_mo_zs_enable")
    public boolean pSOaMoZsEnable;

    @SerializedName("bs_sr_wk_enabled")
    public boolean bsSrWkEnabled;

    @SerializedName("fd_ss_ts_number")
    public int fdSsTsNumber;

    @SerializedName("sb_pe_gd_re_us_enabled")
    public boolean sbPeGdReUsEnabled;

    @SerializedName("color_night_mode_lpd_all")
    public boolean colorNightModeLpdAll;

    @SerializedName("alarm_reminders_enabled")
    public boolean alarmRemindersEnabled;

    @SerializedName("cc_update_phone_enabled")
    public boolean ccUpdatePhoneEnabled;

    @SerializedName("2v_tr_enabled")
    public boolean jsonMember2vTrEnabled;

    @SerializedName("ring_doorbox_enabled")
    public boolean ringDoorboxEnabled;

    @SerializedName("mn_wd_br_te_enabled")
    public boolean mnWdBrTeEnabled;

    @SerializedName("sw_oc_mn_v2_enabled")
    public boolean swOcMnV2Enabled;

    @SerializedName("ch_enabled")
    public boolean chEnabled;

    @SerializedName("sync_vod_dpd_enabled")
    public boolean syncVodDpdEnabled;

    @SerializedName("rp_ey_enabled")
    public boolean rpEyEnabled;

    @SerializedName("ky_2_rg_1_1_enabled")
    public boolean ky2Rg11Enabled;

    @SerializedName("mdlr_drb_2")
    public boolean mdlrDrb2;

    @SerializedName("ky_2_rg_ca_enabled")
    public boolean ky2RgCaEnabled;

    @SerializedName("vl_cx_me_ps_enabled")
    public boolean vlCxMePsEnabled;

    @SerializedName("e2_su_ca_du_enabled")
    public boolean e2SuCaDuEnabled;

    @SerializedName("de_pe_ps_v2_enabled")
    public boolean dePePsV2Enabled;

    @SerializedName("mn_wg_dd_enabled")
    public boolean mnWgDdEnabled;

    @SerializedName("de_lt_enabled")
    public boolean deLtEnabled;

    @SerializedName("rs.common.asset.compatible.whitelist.verizon")
    public boolean rsCommonAssetCompatibleWhitelistVerizon;

    @SerializedName("sr_by_w_enabled")
    public boolean srByWEnabled;

    @SerializedName("gelato_nlsched")
    public boolean gelatoNlsched;

    @SerializedName("RSL CUSTOM SENSITIVITY ENABLED")
    public boolean rSLCUSTOMSENSITIVITYENABLED;

    @SerializedName("cg_in_ce_enabled")
    public boolean cgInCeEnabled;

    @SerializedName("sa_ir_re_enabled")
    public boolean saIrReEnabled;

    @SerializedName("ye_bx_enabled")
    public boolean yeBxEnabled;

    @SerializedName("sr_tt_enabled")
    public boolean srTtEnabled;

    @SerializedName("pt_lp_s1_enabled")
    public boolean ptLpS1Enabled;

    @SerializedName("floodlight_cam_enabled")
    public boolean floodlightCamEnabled;

    @SerializedName("cr_ct_wb_enabled")
    public boolean crCtWbEnabled;

    @SerializedName("btr_sl_schdls_v2_enabled")
    public boolean btrSlSchdlsV2Enabled;

    @SerializedName("nh_case_resolution_enabled")
    public boolean nhCaseResolutionEnabled;

    @SerializedName("mn_ad_gs_enabled")
    public boolean mnAdGsEnabled;

    @SerializedName("nw_de_rh_ps_enabled")
    public boolean nwDeRhPsEnabled;

    @SerializedName("rs_lv_string")
    public String rsLvString = "";

    @SerializedName("et_hy_21_enabled")
    public boolean etHy21Enabled;

    @SerializedName("ps_an_ff2_enabled")
    public boolean psAnFf2Enabled;

    @SerializedName("cocoa_motion_battery_optimization_enabled")
    public boolean cocoaMotionBatteryOptimizationEnabled;

    @SerializedName("alarm_sweden_enabled")
    public boolean alarmSwedenEnabled;

    @SerializedName("linked_devices_beams_tile_enabled")
    public boolean linkedDevicesBeamsTileEnabled;

    @SerializedName("tn_fr_sn_tl_rn_enabled")
    public boolean tnFrSnTlRnEnabled;

    @SerializedName("kbo_enabled")
    public boolean kboEnabled;

    @SerializedName("ky_hr_py_enabled")
    public boolean kyHrPyEnabled;

    @SerializedName("un_ir_aa_ct_enabled")
    public boolean unIrAaCtEnabled;

    @SerializedName("btr_sl_schdls_enabled")
    public boolean btrSlSchdlsEnabled;

    @SerializedName("answer_prestreaming_enabled")
    public boolean answerPrestreamingEnabled;

    @SerializedName("ale_2w_com_enabled")
    public boolean ale2wComEnabled;

    @SerializedName("cd_bd_sa_1_enabled")
    public boolean cdBdSa1Enabled;

    @SerializedName("or_enabled")
    public boolean orEnabled;

    @SerializedName("show_vod_settings")
    public boolean showVodSettings;

    @SerializedName("we_rc_an_device_types")
    public List<Object> weRcAnDeviceTypes = List.of();

    @SerializedName("sort_ugc_text_enabled")
    public boolean sortUgcTextEnabled;

    @SerializedName("sw_oo_setup_enabled")
    public boolean swOoSetupEnabled;

    @SerializedName("rp_wd_enabled")
    public boolean rpWdEnabled;

    @SerializedName("chime_pro_enabled")
    public boolean chimeProEnabled;

    @SerializedName("no_st_ff2_enabled")
    public boolean noStFf2Enabled;

    @SerializedName("dm_vl_enabled")
    public boolean dmVlEnabled;

    @SerializedName("wc_pk_cs_enabled")
    public boolean wcPkCsEnabled;

    @SerializedName("wc_pk_cs_tna_f2_enabled")
    public boolean wcPkCsTnaF2Enabled;

    @SerializedName("sp_me_v_mn_v2_device_types")
    public List<Object> spMeVMnV2DeviceTypes = List.of();

    @SerializedName("rn_opt_in_jn_enabled")
    public boolean rnOptInJnEnabled;

    @SerializedName("ky_2_rg_di_enabled")
    public boolean ky2RgDiEnabled;

    @SerializedName("it_dh_in_ff2_enabled")
    public boolean itDhInFf2Enabled;

    @SerializedName("me_se_nn_am_enabled")
    public boolean meSeNnAmEnabled;

    @SerializedName("alarm_sensitive_strips_enabled")
    public boolean alarmSensitiveStripsEnabled;

    @SerializedName("timeline_force_enabled")
    public boolean timelineForceEnabled;

    @SerializedName("tile_dashboard_mode")
    public String tileDashboardMode = "";

    @SerializedName("aa_sl_cx_enabled")
    public boolean aaSlCxEnabled;

    @SerializedName("sk_gy_enabled")
    public boolean skGyEnabled;

    @SerializedName("wc_ll_mx_sk_rt_pr_sd_number")
    public int wcLlMxSkRtPrSdNumber;

    @SerializedName("by_enabled")
    public boolean byEnabled;

    @SerializedName("sd_pn_fm_bd_enabled")
    public boolean sdPnFmBdEnabled;

    @SerializedName("ld_v4_enabled")
    public boolean ldV4Enabled;

    @SerializedName("ap_jn_ds_enabled")
    public boolean apJnDsEnabled;

    @SerializedName("gelato_v2_enabled")
    public boolean gelatoV2Enabled;

    @SerializedName("sidewalk_feature_enabled")
    public boolean sidewalkFeatureEnabled;

    @SerializedName("be_jy_mg_sp_enabled")
    public boolean beJyMgSpEnabled;

    @SerializedName("multiple_calls_enabled")
    public boolean multipleCallsEnabled;

    @SerializedName("pt_lp_local_enabled")
    public boolean ptLpLocalEnabled;

    @SerializedName("hdr_enabled_lunar")
    public boolean hdrEnabledLunar;

    @SerializedName("mq_sl_enabled")
    public boolean mqSlEnabled;

    @SerializedName("cocoa_camera_enabled")
    public boolean cocoaCameraEnabled;

    @SerializedName("rd_pll_ab")
    public boolean rdPllAb;

    @SerializedName("pl_fe_dc_enabled")
    public boolean plFeDcEnabled;

    @SerializedName("ring_cash_eligible_enabled")
    public boolean ringCashEligibleEnabled;

    @SerializedName("tile_dashboard_enabled")
    public boolean tileDashboardEnabled;

    @SerializedName("mn_zs_rn_device_types")
    public List<String> mnZsRnDeviceTypes = List.of();

    @SerializedName("cd_bd_sa_4a_enabled")
    public boolean cdBdSa4aEnabled;

    @SerializedName("cl_as_enabled")
    public boolean clAsEnabled;

    @SerializedName("de_pe_ps_ff4_device_types")
    public List<Object> dePePsFf4DeviceTypes = List.of();

    @SerializedName("zs_enabled")
    public boolean zsEnabled;

    @SerializedName("a_g_c_l_device_types")
    public List<Object> aGCLDeviceTypes = List.of();

    @SerializedName("sk_enabled")
    public boolean skEnabled;

    @SerializedName("flood_freeze_reminders_new_features_enabled")
    public boolean floodFreezeRemindersNewFeaturesEnabled;

    @SerializedName("pr_or_et_sh_enabled")
    public boolean prOrEtShEnabled;

    @SerializedName("hy_sg_enabled")
    public boolean hySgEnabled;

    @SerializedName("mdlr_chm_p")
    public boolean mdlrChmP;

    @SerializedName("ge_gd_enabled")
    public boolean geGdEnabled;

    @SerializedName("aed_by_cg_enabled")
    public boolean aedByCgEnabled;

    @SerializedName("pe_lk_enabled")
    public boolean peLkEnabled;

    @SerializedName("in_app_call_notifications")
    public boolean inAppCallNotifications;

    @SerializedName("nt_gn_et_sm_enabled")
    public boolean ntGnEtSmEnabled;

    @SerializedName("mdlr_orn")
    public boolean mdlrOrn;

    @SerializedName("wired_hw_h264_decoder_enabled")
    public boolean wiredHwH264DecoderEnabled;

    @SerializedName("linked_devices_modes_enabled")
    public boolean linkedDevicesModesEnabled;

    @SerializedName("device_traits_enabled_inc")
    public boolean deviceTraitsEnabledInc;

    @SerializedName("rh_tn_no_nt_ln_ff2_enabled")
    public boolean rhTnNoNtLnFf2Enabled;

    @SerializedName("nh_mu_lo_enabled")
    public boolean nhMuLoEnabled;

    @SerializedName("sg_sk_mx_sr_enabled")
    public boolean sgSkMxSrEnabled;

    @SerializedName("sm_io_pn3_enabled")
    public boolean smIoPn3Enabled;

    @SerializedName("linked_devices_enabled")
    public boolean linkedDevicesEnabled;

    @SerializedName("native_lock_unlock")
    public boolean nativeLockUnlock;

    @SerializedName("mmfa_enabled")
    public boolean mmfaEnabled;

    @SerializedName("ko_rl_device_types")
    public List<String> koRlDeviceTypes = List.of();

    @SerializedName("cocoa_doorbell_enabled")
    public boolean cocoaDoorbellEnabled;

    @SerializedName("lpd_enabled")
    public boolean lpdEnabled;

    @SerializedName("kj_ip_sp_enabled")
    public boolean kjIpSpEnabled;

    @SerializedName("p_s_oa_po_zs_enabled")
    public boolean pSOaPoZsEnabled;

    @SerializedName("aed_co_se_am_enabled")
    public boolean aedCoSeAmEnabled;

    @SerializedName("test-bug-bash-gradual-rollout-wangmae-array")
    public List<String> testBugBashGradualRolloutWangmaeArray = List.of();

    @SerializedName("aed_gs_bk_enabled")
    public boolean aedGsBkEnabled;

    @SerializedName("timeline_adoption_endgame_enabled")
    public boolean timelineAdoptionEndgameEnabled;

    @SerializedName("pt_lp_s2_enabled")
    public boolean ptLpS2Enabled;

    @SerializedName("post_setup_flow_enabled")
    public boolean postSetupFlowEnabled;

    @SerializedName("cd_enabled")
    public boolean cdEnabled;

    @SerializedName("stickupcam_setup_enabled")
    public boolean stickupcamSetupEnabled;

    @SerializedName("cw_enabled")
    public boolean cwEnabled;

    @SerializedName("scrubber_enabled")
    public boolean scrubberEnabled;

    @SerializedName("rp_sd_an_enabled")
    public boolean rpSdAnEnabled;

    @SerializedName("ig_pn_ad_ve_enabled")
    public int igPnAdVeEnabled;

    @SerializedName("aa_sg_v2_enabled")
    public boolean aaSgV2Enabled;

    @SerializedName("wc_pk_cs_tna_enabled")
    public boolean wcPkCsTnaEnabled;

    @SerializedName("hp_ne_enabled")
    public boolean hpNeEnabled;

    @SerializedName("ms_wt_ss_enabled")
    public boolean msWtSsEnabled;

    @SerializedName("doorbell_portal_enabled")
    public boolean doorbellPortalEnabled;

    @SerializedName("fe_am_ap_cl_enabled")
    public boolean feAmApClEnabled;

    @SerializedName("cl_cd_me_mu_v2_enabled")
    public boolean clCdMeMuV2Enabled;

    @SerializedName("privacy_settings")
    public boolean privacySettings;

    @SerializedName("ao_cp_lh_device_types")
    public List<String> aoCpLhDeviceTypes = List.of();

    @SerializedName("ad_go_dy_ky_ms_enabled")
    public boolean adGoDyKyMsEnabled;

    @SerializedName("string")
    public String string = "";

    @SerializedName("pt_lw_rg_il")
    public int ptLwRgIl;

    @SerializedName("ring_solar_beams_device_types_enabled")
    public List<String> ringSolarBeamsDeviceTypesEnabled = List.of();

    @SerializedName("ge_dg_me_enabled")
    public boolean geDgMeEnabled;

    @SerializedName("mldr_chm")
    public boolean mldrChm;

    @SerializedName("pt_lp_2_enabled")
    public boolean ptLp2Enabled;

    @SerializedName("tg_nk_ay_enabled")
    public boolean tgNkAyEnabled;

    @SerializedName("vo_vn_am_enabled")
    public boolean voVnAmEnabled;

    @SerializedName("he_pe_lk_enabled")
    public boolean hePeLkEnabled;

    @SerializedName("ringplus_enabled")
    public boolean ringplusEnabled;

    @SerializedName("motion_snoozing_enabled")
    public boolean motionSnoozingEnabled;

    @SerializedName("gi_enabled")
    public boolean giEnabled;

    @SerializedName("mdlr_smr_38")
    public boolean mdlrSmr38;

    @SerializedName("st_vo_ds_vo_enabled")
    public boolean stVoDsVoEnabled;

    @SerializedName("mdlr_smr_35")
    public boolean mdlrSmr35;

    @SerializedName("nw_enc_flw_enabled")
    public boolean nwEncFlwEnabled;

    @SerializedName("on_dr_ag_enabled")
    public boolean onDrAgEnabled;

    @SerializedName("cc_update_password_enabled")
    public boolean ccUpdatePasswordEnabled;

    @SerializedName("alarm_finland_enabled")
    public boolean alarmFinlandEnabled;

    @SerializedName("js_cl_dn_me_enabled")
    public boolean jsClDnMeEnabled;

    @SerializedName("nh_se_ee_is_enabled")
    public boolean nhSeEeIsEnabled;

    @SerializedName("on_be_st_enabled")
    public boolean onBeStEnabled;

    @SerializedName("ringtones_enabled")
    public boolean ringtonesEnabled;

    @SerializedName("ad_pn_tt_enabled")
    public boolean adPnTtEnabled;

    @SerializedName("ns_v2_enabled")
    public boolean nsV2Enabled;

    @SerializedName("le_lp_42_enabled")
    public boolean leLp42Enabled;

    @SerializedName("as_mn_na_enabled")
    public boolean asMnNaEnabled;

    @SerializedName("md_am_fs_enabled")
    public boolean mdAmFsEnabled;

    @SerializedName("avp_slq_enabled")
    public boolean avpSlqEnabled;

    @SerializedName("ls_v2_enabled")
    public boolean lsV2Enabled;

    @SerializedName("an_ky_po_enabled")
    public boolean anKyPoEnabled;

    @SerializedName("rs.devicecatalog.whitelist.device.gbrs")
    public boolean rsDevicecatalogWhitelistDeviceGbrs;

    @SerializedName("aa_fr_yu_rd_dt_ir_enabled")
    public boolean aaFrYuRdDtIrEnabled;

    @SerializedName("ai_hd_kt_in_enabled")
    public boolean aiHdKtInEnabled;

    @SerializedName("im_dg_cs_enabled")
    public boolean imDgCsEnabled;

    @SerializedName("rs.devicecatalog.allowlist.device.baldeagle")
    public boolean rsDevicecatalogAllowlistDeviceBaldeagle;

    @SerializedName("be_jy_lw_ll_co_enabled")
    public boolean beJyLwLlCoEnabled;

    @SerializedName("le_te_20_enabled")
    public boolean leTe20Enabled;

    @SerializedName("ms_ag_enabled")
    public boolean msAgEnabled;

    @SerializedName("mdlr_sclp_nov")
    public boolean mdlrSclpNov;

    @SerializedName("ff2_test_before_prod_launch_19_04_2024")
    public boolean ff2TestBeforeProdLaunch19042024;

    @SerializedName("ch_plnt_tst_device_types")
    public List<String> chPlntTstDeviceTypes = List.of();

    @SerializedName("cocoa_ignore_zones_enabled")
    public boolean cocoaIgnoreZonesEnabled;

    @SerializedName("ff_24x7_lite_frequency_switch")
    public boolean ff24x7LiteFrequencySwitch;

    @SerializedName("mdlr_drb_hzl_35")
    public boolean mdlrDrbHzl35;

    @SerializedName("sw_oc_lgts_enabled")
    public boolean swOcLgtsEnabled;

    @SerializedName("john_wiz_enabled")
    public boolean johnWizEnabled;

    @SerializedName("review_prompt_enabled")
    public boolean reviewPromptEnabled;

    @SerializedName("garys_first")
    public boolean garysFirst;

    @SerializedName("slr_smt_lts_brtss_lvl_v2_enabled")
    public boolean slrSmtLtsBrtssLvlV2Enabled;

    @SerializedName("pt_vs_ha_enabled")
    public boolean ptVsHaEnabled;

    @SerializedName("dl_og_hp_enabled")
    public boolean dlOgHpEnabled;

    @SerializedName("2v_pe_3_x_enabled")
    public boolean jsonMember2vPe3XEnabled;

    @SerializedName("eo_as_sn_p1_enabled")
    public boolean eoAsSnP1Enabled;

    @SerializedName("test-bug-bash-gradual-rollout-zmrcosg-string")
    public String testBugBashGradualRolloutZmrcosgString = "";

    @SerializedName("pt_lw_rg_il_enabled")
    public boolean ptLwRgIlEnabled;

    @SerializedName("dd_de_ts_enabled")
    public boolean ddDeTsEnabled;

    @SerializedName("pe_re_cn_enabled")
    public boolean peReCnEnabled;

    @SerializedName("cs_ln_cs_enabled")
    public boolean csLnCsEnabled;

    @SerializedName("aa_sg_v2")
    public boolean aaSgV2;

    @SerializedName("rh_tn_no_nt_ln_enabled")
    public boolean rhTnNoNtLnEnabled;

    @SerializedName("rs.devicecatalog.allowlist.device.yale500yrd420")
    public boolean rsDevicecatalogAllowlistDeviceYale500yrd420;

    @SerializedName("cc_update_name_enabled")
    public boolean ccUpdateNameEnabled;

    @SerializedName("ed_et_sh_enabled")
    public boolean edEtShEnabled;

    @SerializedName("vod_enabled")
    public boolean vodEnabled;

    @SerializedName("dl_pr_ms_v2_enabled")
    public boolean dlPrMsV2Enabled;

    @SerializedName("ld_ss_ce_po1_enabled")
    public boolean ldSsCePo1Enabled;

    @SerializedName("rs.devicecatalog.whitelist.device.gen2eu")
    public boolean rsDevicecatalogWhitelistDeviceGen2eu;

    @SerializedName("qk_cs_1_pt_enabled")
    public boolean qkCs1PtEnabled;

    @SerializedName("ty_sn_rg_device_types")
    public List<Object> tySnRgDeviceTypes = List.of();

    @SerializedName("se_ps_enabled")
    public boolean sePsEnabled;

    @SerializedName("ct_es_enabled")
    public boolean ctEsEnabled;

    @SerializedName("cl_as_ff3_enabled")
    public boolean clAsFf3Enabled;

    @SerializedName("rpp_menu_enabled")
    public boolean rppMenuEnabled;

    @SerializedName("test qa")
    public boolean testQa;

    @SerializedName("aed_ff2_enabled")
    public boolean aedFf2Enabled;

    @SerializedName("dashboard_control")
    public boolean dashboardControl;

    @SerializedName("he_jf_3d_pt_sp_enabled")
    public boolean heJf3dPtSpEnabled;

    @SerializedName("rs.devicecatalog.allowlist.device.yale500yrd410")
    public boolean rsDevicecatalogAllowlistDeviceYale500yrd410;

    @SerializedName("ss_sp_ff1_enabled")
    public boolean ssSpFf1Enabled;

    @SerializedName("nw_reply_to_comments_enabled")
    public boolean nwReplyToCommentsEnabled;

    @SerializedName("owner_proactive_snoozing_enabled")
    public boolean ownerProactiveSnoozingEnabled;

    @SerializedName("ay_as_sn_py_cd_enabled")
    public boolean ayAsSnPyCdEnabled;

    @SerializedName("chime_v2_enabled")
    public boolean chimeV2Enabled;

    @SerializedName("nw_crime_recap_share_enabled")
    public boolean nwCrimeRecapShareEnabled;

    @SerializedName("nw_ca_ts_enabled")
    public boolean nwCaTsEnabled;

    @SerializedName("et_hy_nw_enabled")
    public boolean etHyNwEnabled;

    @SerializedName("spotlight_battery_dashboard_controls_enabled")
    public boolean spotlightBatteryDashboardControlsEnabled;

    @SerializedName("js_mn_vn_enabled")
    public boolean jsMnVnEnabled;

    @SerializedName("fh_tr_enabled")
    public boolean fhTrEnabled;

    @SerializedName("ed_ce_bu_enabled")
    public boolean edCeBuEnabled;

    @SerializedName("s2_zw_enabled")
    public boolean s2ZwEnabled;

    @SerializedName("nw_case_resolve_enabled")
    public boolean nwCaseResolveEnabled;

    @SerializedName("stickup_cam_mini_enabled")
    public boolean stickupCamMiniEnabled;

    @SerializedName("se_lk_enabled")
    public boolean seLkEnabled;

    @SerializedName("an_na_sg_sn_rt_enabled")
    public boolean anNaSgSnRtEnabled;

    @SerializedName("brd_enabled")
    public boolean brdEnabled;

    @SerializedName("js_enabled")
    public boolean jsEnabled;

    @SerializedName("rs.devicecatalog.allowlist.device.yale500yrd450")
    public boolean rsDevicecatalogAllowlistDeviceYale500yrd450;

    @SerializedName("p_s_oa_pr_zs_enabled")
    public boolean pSOaPrZsEnabled;

    @SerializedName("nd_sd_enabled")
    public boolean ndSdEnabled;

    @SerializedName("rs.devicecatalog.whitelist.device.gen2devices")
    public boolean rsDevicecatalogWhitelistDeviceGen2devices;

    @SerializedName("nh_referral_v2_ring_core_enabled")
    public boolean nhReferralV2RingCoreEnabled;

    @SerializedName("unverified_address_V2_enabled")
    public boolean unverifiedAddressV2Enabled;

    @SerializedName("swk_ct_enabled")
    public boolean swkCtEnabled;

    @SerializedName("ko_enabled")
    public boolean koEnabled;

    @SerializedName("aa_ay_tb_p4_enabled")
    public boolean aaAyTbP4Enabled;

    @SerializedName("set_as_device_types")
    public List<String> setAsDeviceTypes = List.of();

    @SerializedName("pt_lp_sd_andr_enabled")
    public boolean ptLpSdAndrEnabled;

    @SerializedName("ay_an_as_sn_enabled")
    public boolean ayAnAsSnEnabled;

    @SerializedName("alarm_ivr_on_cancel")
    public boolean alarmIvrOnCancel;

    @SerializedName("ta_pn_enabled")
    public boolean taPnEnabled;

    @SerializedName("adpv_pk_enabled")
    public boolean adpvPkEnabled;

    @SerializedName("ts_jf_enabled")
    public boolean tsJfEnabled;

    @SerializedName("lw_le_gb_ab_enabled")
    public boolean lwLeGbAbEnabled;

    @SerializedName("cd_bd_sa_1a_enabled")
    public boolean cdBdSa1aEnabled;

    @SerializedName("mdlr_prtl")
    public boolean mdlrPrtl;

    @SerializedName("rs.devicecatalog.allowlist.device.yale500yrd430")
    public boolean rsDevicecatalogAllowlistDeviceYale500yrd430;

    @SerializedName("oa_enabled")
    public boolean oaEnabled;

    @SerializedName("dashboard_help_tile_enabled")
    public boolean dashboardHelpTileEnabled;

    @SerializedName("beams_custom_sensitivity_enabled")
    public boolean beamsCustomSensitivityEnabled;

    @SerializedName("camera_groups_enabled")
    public boolean cameraGroupsEnabled;

    @SerializedName("amazon_key_setup")
    public boolean amazonKeySetup;

    @SerializedName("sb_pe_gd_re_uk_enabled")
    public boolean sbPeGdReUkEnabled;

    @SerializedName("motion_message_enabled")
    public boolean motionMessageEnabled;

    @SerializedName("remote_logging_format_storing")
    public boolean remoteLoggingFormatStoring;

    @SerializedName("cfes_2_enabled")
    public boolean cfes2Enabled;

    @SerializedName("sclp_sns_pp_enabled")
    public boolean sclpSnsPpEnabled;

    @SerializedName("ag_enabled")
    public boolean agEnabled;

    @SerializedName("sf_se_de_op_tr_2_enabled")
    public boolean sfSeDeOpTr2Enabled;

    @SerializedName("gb_arpl_device_types")
    public List<String> gbArplDeviceTypes = List.of();

    @SerializedName("subs_attachment_enabled")
    public boolean subsAttachmentEnabled;

    @SerializedName("sp_me_v_mn_device_types")
    public List<Object> spMeVMnDeviceTypes = List.of();

    @SerializedName("au_nz_arpl_device_types")
    public List<String> auNzArplDeviceTypes = List.of();

    @SerializedName("emp_ff2_enabled")
    public boolean empFf2Enabled;

    @SerializedName("dg_cs_enabled")
    public boolean dgCsEnabled;

    @SerializedName("pt_lp_grsrt_enabled")
    public boolean ptLpGrsrtEnabled;

    @SerializedName("audio_recording")
    public boolean audioRecording;

    @SerializedName("pt_lp_ls_enabled")
    public boolean ptLpLsEnabled;

    @SerializedName("control_external_lights_enabled")
    public boolean controlExternalLightsEnabled;

    @SerializedName("dashboard_ms")
    public boolean dashboardMs;

    @SerializedName("ll_lk_enabled")
    public boolean llLkEnabled;

    @SerializedName("key_delivery_clip_filter_enabled")
    public boolean keyDeliveryClipFilterEnabled;

    @SerializedName("st_vo_ds_enabled")
    public boolean stVoDsEnabled;

    @SerializedName("ko_bu_re_enabled")
    public boolean koBuReEnabled;

    @SerializedName("st_as_v2_device_types")
    public List<String> stAsV2DeviceTypes = List.of();

    @SerializedName("aed_dg_bg_enabled")
    public boolean aedDgBgEnabled;

    @SerializedName("te_bd_ae_tr_v2_enabled")
    public boolean teBdAeTrV2Enabled;

    @SerializedName("dashboard_horizontal_scrolling_enabled")
    public boolean dashboardHorizontalScrollingEnabled;

    @SerializedName("aa_st_te_fr_yu_v2_enabled")
    public boolean aaStTeFrYuV2Enabled;

    @SerializedName("cont_batt_tip_enabled")
    public boolean contBattTipEnabled;

    @SerializedName("video_request_enabled")
    public boolean videoRequestEnabled;

    @SerializedName("lw_ct_ie_nk_py_io_enabled")
    public boolean lwCtIeNkPyIoEnabled;

    @SerializedName("rp_cx_enabled")
    public boolean rpCxEnabled;

    @SerializedName("vo_fk_11_enabled")
    public boolean voFk11Enabled;

    @SerializedName("ms_15_enabled")
    public boolean ms15Enabled;

    @SerializedName("wwr_amazon_onboarding_enabled_inc")
    public boolean wwrAmazonOnboardingEnabledInc;

    @SerializedName("doorbell_v2_enabled")
    public boolean doorbellV2Enabled;

    @SerializedName("lp_fg_enabled")
    public boolean lpFgEnabled;

    @SerializedName("lv_sm_pp_enabled")
    public boolean lvSmPpEnabled;

    @SerializedName("de_sn_v2_enabled")
    public boolean deSnV2Enabled;

    @SerializedName("light_groups_enabled")
    public boolean lightGroupsEnabled;

    @SerializedName("3p_cm_am_enabled")
    public boolean jsonMember3pCmAmEnabled;

    @SerializedName("vrl_dpd_enabled")
    public boolean vrlDpdEnabled;

    @SerializedName("tline_no_gaps_smr_enabled")
    public boolean tlineNoGapsSmrEnabled;

    @SerializedName("pip_enabled")
    public boolean pipEnabled;

    @SerializedName("alarm_web_blink_enabled")
    public boolean alarmWebBlinkEnabled;

    @SerializedName("nw_notification_types_enabled")
    public boolean nwNotificationTypesEnabled;

    @SerializedName("malformed_email")
    public boolean malformedEmail;

    @SerializedName("pt_pn_tl_sp_enabled")
    public boolean ptPnTlSpEnabled;

    @SerializedName("timeline_force_enable")
    public boolean timelineForceEnable;

    @SerializedName("he_la_ky_lk_enabled")
    public boolean heLaKyLkEnabled;

    @SerializedName("fl_wtr_vlv_ft")
    public boolean flWtrVlvFt;

    @SerializedName("zz_tn_wr_sf_ve_enabled")
    public boolean zzTnWrSfVeEnabled;

    @SerializedName("cl_cd_mn_dn_enabled")
    public boolean clCdMnDnEnabled;

    @SerializedName("nh_it_mn_st_enabled")
    public boolean nhItMnStEnabled;

    @SerializedName("as_v2_ei_enabled")
    public boolean asV2EiEnabled;

    @SerializedName("sf_se_cy2_enabled")
    public boolean sfSeCy2Enabled;

    @SerializedName("kj_bn_ck_enabled")
    public boolean kjBnCkEnabled;

    @SerializedName("ge_dg_ll_ts_enableda")
    public boolean geDgLlTsEnableda;

    @SerializedName("be_jy_re_se_enabled")
    public boolean beJyReSeEnabled;

    @SerializedName("min_max_enabled_dpd_nonv4")
    public boolean minMaxEnabledDpdNonv4;

    @SerializedName("rpp_attach_site_redirect_enabled")
    public boolean rppAttachSiteRedirectEnabled;

    @SerializedName("advanced_motion_detection_enabled")
    public boolean advancedMotionDetectionEnabled;

    @SerializedName("stickupcam_elite_setup_enabled")
    public boolean stickupcamEliteSetupEnabled;

    @SerializedName("dbgc_enabled")
    public boolean dbgcEnabled;

    @SerializedName("ll_cv_se_enabled")
    public boolean llCvSeEnabled;

    @SerializedName("non_owner_setup_check_enabled")
    public boolean nonOwnerSetupCheckEnabled;

    @SerializedName("ad_go_dy_ky_cn_enabled")
    public boolean adGoDyKyCnEnabled;

    @SerializedName("ps_cc_dk_enabled")
    public boolean psCcDkEnabled;

    @SerializedName("rich_notifications_enabled")
    public boolean richNotificationsEnabled;

    @SerializedName("beams_avtadv_stp_enabled")
    public boolean beamsAvtadvStpEnabled;

    @SerializedName("mdlr_spt")
    public boolean mdlrSpt;

    @SerializedName("cnm_enabled_dpdv4")
    public boolean cnmEnabledDpdv4;

    @SerializedName("giovanni_enabled")
    public boolean giovanniEnabled;

    @SerializedName("ml_enabled")
    public boolean mlEnabled;

    @SerializedName("spotlight_scheduling_enabled")
    public boolean spotlightSchedulingEnabled;

    @SerializedName("es_2_il_st_enabled")
    public boolean es2IlStEnabled;

    @SerializedName("ky_2_rg_1_2_enabled")
    public boolean ky2Rg12Enabled;

    @SerializedName("aa_fr_yu_rd_dy_cd_enabled")
    public boolean aaFrYuRdDyCdEnabled;

    @SerializedName("bu_re_p3_enabled")
    public boolean buReP3Enabled;

    @SerializedName("proactive_snoozing_enabled")
    public boolean proactiveSnoozingEnabled;

    @SerializedName("ca_sw_hf_enabled")
    public boolean caSwHfEnabled;

    @SerializedName("alarm_mode1_enabled")
    public boolean alarmMode1Enabled;

    @SerializedName("alexa_aed_enabled")
    public boolean alexaAedEnabled;

    @SerializedName("mac_no_device_settings_enabled")
    public boolean macNoDeviceSettingsEnabled;

    @SerializedName("nw_larger_area_enabled")
    public boolean nwLargerAreaEnabled;

    @SerializedName("de_pe_ps_v3_enabled")
    public boolean dePePsV3Enabled;

    @SerializedName("ring_for_business_prompt_enabled")
    public boolean ringForBusinessPromptEnabled;

    @SerializedName("history_classification_enabled")
    public boolean historyClassificationEnabled;

    @SerializedName("keypad_chirps_enabled")
    public boolean keypadChirpsEnabled;

    @SerializedName("android_native_websockets_enabled")
    public boolean androidNativeWebsocketsEnabled;

    @SerializedName("js_wd_en_enabled")
    public boolean jsWdEnEnabled;

    @SerializedName("pe_sg_enabled")
    public boolean peSgEnabled;

    @SerializedName("am_mq_sl_enabled")
    public boolean amMqSlEnabled;

    @SerializedName("linked_devices_setup_enabled")
    public boolean linkedDevicesSetupEnabled;

    @SerializedName("we_rc_an_2_device_types")
    public List<String> weRcAn2DeviceTypes = List.of();

    @SerializedName("vrl31_cocoa_enabled")
    public boolean vrl31CocoaEnabled;

    @SerializedName("sh_sh_do_enabled")
    public boolean shShDoEnabled;

    @SerializedName("nn_us_py_fs_pt_sp_device_types")
    public List<String> nnUsPyFsPtSpDeviceTypes = List.of();

    @SerializedName("ld_ss_ce_bc_enabled")
    public boolean ldSsCeBcEnabled;

    @SerializedName("sm_io_pn_enabled")
    public boolean smIoPnEnabled;

    @SerializedName("ky_2_rg_dl_rt_enabled")
    public boolean ky2RgDlRtEnabled;

    @SerializedName("test")
    public int test;

    @SerializedName("mdlr_drb_pro")
    public boolean mdlrDrbPro;

    @SerializedName("sw_oc_enabled")
    public boolean swOcEnabled;

    @SerializedName("be_jy_by_enabled")
    public boolean beJyByEnabled;

    @SerializedName("pt_lp_loc_enabled")
    public boolean ptLpLocEnabled;

    @SerializedName("rp_ce_de_ne_enabled")
    public boolean rpCeDeNeEnabled;

    @SerializedName("cl_cd_mn_dn2_enabled")
    public boolean clCdMnDn2Enabled;

    @SerializedName("affogato_v2_enabled")
    public boolean affogatoV2Enabled;

    @SerializedName("rotate_180_deg_enabled")
    public boolean rotate180DegEnabled;

    @SerializedName("raw_video_enabled")
    public boolean rawVideoEnabled;

    @SerializedName("ff_offline_motion_events_enabled")
    public boolean ffOfflineMotionEventsEnabled;

    @SerializedName("by_pe_mode_enabled")
    public boolean byPeModeEnabled;

    @SerializedName("zs_dg_enabled")
    public boolean zsDgEnabled;

    @SerializedName("sm_in_pn2_enabled")
    public boolean smInPn2Enabled;

    @SerializedName("ce_po_nk_v2_enabled")
    public boolean cePoNkV2Enabled;

    @SerializedName("ay_pt_sp_rr_enabled")
    public boolean ayPtSpRrEnabled;

    @SerializedName("an_ky_lk_fs_hd_enabled")
    public boolean anKyLkFsHdEnabled;

    @SerializedName("ale_db_anoun_enabled")
    public boolean aleDbAnounEnabled;

    @SerializedName("rs.devicecatalog.allowlist.device.zoozvalve")
    public boolean rsDevicecatalogAllowlistDeviceZoozvalve;

    @SerializedName("bn_wpn_enabled")
    public boolean bnWpnEnabled;

    @SerializedName("pt_lp_bt_enabled")
    public boolean ptLpBtEnabled;

    @SerializedName("te_bd_dn_ff2_enabled")
    public boolean teBdDnFf2Enabled;

    @SerializedName("de_re_is_enabled")
    public boolean deReIsEnabled;

    @SerializedName("nh_case_information_enabled")
    public boolean nhCaseInformationEnabled;

    @SerializedName("ty_fr_sn_rg_enabled")
    public boolean tyFrSnRgEnabled;

    @SerializedName("x_line_enabled")
    public boolean xLineEnabled;

    @SerializedName("cn_me_fr_be_gs_enabled")
    public boolean cnMeFrBeGsEnabled;

    @SerializedName("ring_cam_mount_enabled")
    public boolean ringCamMountEnabled;

    @SerializedName("pt_lp_laas_enabled")
    public boolean ptLpLaasEnabled;

    @SerializedName("pt_vs_hi_enabled")
    public boolean ptVsHiEnabled;

    @SerializedName("ring_for_business_enabled")
    public boolean ringForBusinessEnabled;

    @SerializedName("accessories_upsell_1_0_enabled")
    public boolean accessoriesUpsell10Enabled;

    @SerializedName("cd_bd_sa_1pa_enabled")
    public boolean cdBdSa1paEnabled;

    @SerializedName("le_vw_le20_enabled")
    public boolean leVwLe20Enabled;

    @SerializedName("mn_wg_ca_enabled")
    public boolean mnWgCaEnabled;

    @SerializedName("go_dy_ky_cn_enabled")
    public boolean goDyKyCnEnabled;

    @SerializedName("mdlr_smr")
    public boolean mdlrSmr;

    @SerializedName("mx_st_enabled")
    public boolean mxStEnabled;

    @SerializedName("st_ce_ps_enabled")
    public boolean stCePsEnabled;

    @SerializedName("pe_wg_v1_enabled")
    public boolean peWgV1Enabled;

    @SerializedName("ky_2_rg_enabled")
    public boolean ky2RgEnabled;

    @SerializedName("pl_mg_fr_cs_enabled")
    public boolean plMgFrCsEnabled;

    @SerializedName("cd_bd_sa_p1a_enabled")
    public boolean cdBdSaP1aEnabled;

    @SerializedName("aa_cr_ct_enabled")
    public boolean aaCrCtEnabled;

    @SerializedName("min_max_enabled_dpd_v4")
    public boolean minMaxEnabledDpdV4;

    @SerializedName("go_be_sp_enabled")
    public boolean goBeSpEnabled;

    @SerializedName("lpd_motion_announcement_enabled")
    public boolean lpdMotionAnnouncementEnabled;

    @SerializedName("vl_cx_gt_as_enabled")
    public boolean vlCxGtAsEnabled;

    @SerializedName("show_red_tails_toggle")
    public boolean showRedTailsToggle;

    @SerializedName("vrl_cocoa_enabled")
    public boolean vrlCocoaEnabled;

    @SerializedName("we_si_4_an_device_types")
    public List<String> weSi4AnDeviceTypes = List.of();

    @SerializedName("rg_hp_rn_abt_enabled")
    public boolean rgHpRnAbtEnabled;

    @SerializedName("ko_gs_sg_enabled")
    public boolean koGsSgEnabled;

    @SerializedName("afu_enabled")
    public boolean afuEnabled;

    @SerializedName("rs.devicecatalog.whitelist.device.kwickset500")
    public boolean rsDevicecatalogWhitelistDeviceKwickset500;

    @SerializedName("onetwothreetest")
    public boolean onetwothreetest;

    @SerializedName("privacy_zones_device_types")
    public List<String> privacyZonesDeviceTypes = List.of();

    @SerializedName("cl_as_ff5_enabled")
    public boolean clAsFf5Enabled;

    @SerializedName("chm_sv2_enabled")
    public boolean chmSv2Enabled;

    @SerializedName("alarm_iceland_enabled")
    public boolean alarmIcelandEnabled;

    @SerializedName("es_1_inl_st_enabled")
    public boolean es1InlStEnabled;

    @SerializedName("ky_2_rg_it_ap_enabled")
    public boolean ky2RgItApEnabled;

    @SerializedName("fast_app_start_caching_disabled")
    public boolean fastAppStartCachingDisabled;

    @SerializedName("leo_device_setup_unlock_enabled")
    public boolean leoDeviceSetupUnlockEnabled;

    @SerializedName("light_schedule_wizard_enabled")
    public boolean lightScheduleWizardEnabled;

    @SerializedName("sm_ch_auto_detection_enabled")
    public boolean smChAutoDetectionEnabled;

    @SerializedName("min_max_enabled_dpd_lunar")
    public boolean minMaxEnabledDpdLunar;

    @SerializedName("cc_update_email_enabled")
    public boolean ccUpdateEmailEnabled;

    @SerializedName("ld_sir_enabled")
    public boolean ldSirEnabled;

    @SerializedName("ns_v2_2_enabled")
    public boolean nsV22Enabled;

    @SerializedName("js_hr_sp_enabled")
    public boolean jsHrSpEnabled;

    @SerializedName("dispatch_monitoring_v2_enabled")
    public boolean dispatchMonitoringV2Enabled;

    @SerializedName("py_cd_wr_enabled")
    public boolean pyCdWrEnabled;

    @SerializedName("rs.devicecatalog.allowlist.device.firstalert500smco410")
    public boolean rsDevicecatalogAllowlistDeviceFirstalert500smco410;

    @SerializedName("ta_pl_enabled")
    public boolean taPlEnabled;

    @SerializedName("timeline_adopt_p1_enabled")
    public boolean timelineAdoptP1Enabled;

    @SerializedName("loitering_detection_device_types")
    public List<String> loiteringDetectionDeviceTypes = List.of();

    @SerializedName("linked_devices_ring_tile_enabled")
    public boolean linkedDevicesRingTileEnabled;

    @SerializedName("te_bd_dn_enabled")
    public boolean teBdDnEnabled;

    @SerializedName("ho_20_gs_device_types")
    public List<String> ho20GsDeviceTypes = List.of();

    @SerializedName("it_dh_in_enabled")
    public boolean itDhInEnabled;

    @SerializedName("ambient_light_sensor_lpd_enabled")
    public boolean ambientLightSensorLpdEnabled;

    @SerializedName("hy_ss_20_enabled")
    public boolean hySs20Enabled;

    @SerializedName("pt_lw_rg_il_v2")
    public int ptLwRgIlV2;

    @SerializedName("e2_su_ca_it_enabled")
    public boolean e2SuCaItEnabled;

    @SerializedName("ge_dg_ll_ts_enabled")
    public boolean geDgLlTsEnabled;

    @SerializedName("js_mn_zn_enabled")
    public boolean jsMnZnEnabled;

    @SerializedName("rg_hp_rn_enabled")
    public boolean rgHpRnEnabled;

    @SerializedName("group_sharing_v3_enabled")
    public boolean groupSharingV3Enabled;

    @SerializedName("ca_be_sp_device_types")
    public List<String> caBeSpDeviceTypes = List.of();

    @SerializedName("mq_gd_ft")
    public boolean mqGdFt;

    @SerializedName("lt_pt_me_te_enabled")
    public boolean ltPtMeTeEnabled;

    @SerializedName("is_ne_ws_enabled")
    public boolean isNeWsEnabled;

    @SerializedName("sw_cn_gp_enabled")
    public boolean swCnGpEnabled;

    @SerializedName("multiple_delete_enabled")
    public boolean multipleDeleteEnabled;

    @SerializedName("gl_br_tt_enabled")
    public boolean glBrTtEnabled;

    @SerializedName("br_ld_ns_enabled")
    public boolean brLdNsEnabled;

    @SerializedName("me_ss_enabled")
    public boolean meSsEnabled;

    @SerializedName("sa_me_cr_enabled")
    public boolean saMeCrEnabled;

    @SerializedName("ll_ay_ss_enabled")
    public boolean llAySsEnabled;

    @SerializedName("hi_uc_bs_we_enabled")
    public boolean hiUcBsWeEnabled;

    @SerializedName("fd_oa_bg_enabled")
    public boolean fdOaBgEnabled;

    @SerializedName("ft_cm_po_38_enabled")
    public boolean ftCmPo38Enabled;

    @SerializedName("rl_gs_on_device_types")
    public List<String> rlGsOnDeviceTypes = List.of();

    @SerializedName("qa_testing_1234")
    public String qaTesting1234 = "";

    @SerializedName("pt_lp_1_enabled")
    public boolean ptLp1Enabled;

    @SerializedName("new_features_2_0_enabled")
    public boolean newFeatures20Enabled;

    @SerializedName("ns_rn_v2_enabled")
    public boolean nsRnV2Enabled;

    @SerializedName("bz_medusa_enabled")
    public boolean bzMedusaEnabled;

    @SerializedName("aa_fr_yu_rd_dy_cd_v2_enabled")
    public boolean aaFrYuRdDyCdV2Enabled;

    @SerializedName("hdr_enabled_dpdv4")
    public boolean hdrEnabledDpdv4;

    @SerializedName("ar_ag_mw_mode_enabled")
    public boolean arAgMwModeEnabled;

    @SerializedName("cn_mg_me_enabled")
    public boolean cnMgMeEnabled;

    @SerializedName("nm_enabled")
    public boolean nmEnabled;

    @SerializedName("p_s_oa_mo_zs_enabled")
    public boolean pSOaMoZsEnabled;

    @SerializedName("sh_cb_ff_enabled")
    public boolean shCbFfEnabled;

    @SerializedName("alarm_flatline_enabled")
    public boolean alarmFlatlineEnabled;

    @SerializedName("lv_sm_pp_an_enabled")
    public boolean lvSmPpAnEnabled;

    @SerializedName("elite_cam_enabled")
    public boolean eliteCamEnabled;

    @SerializedName("alarm_2_0_enabled")
    public boolean alarm20Enabled;

    @SerializedName("ring_cam_battery_enabled")
    public boolean ringCamBatteryEnabled;

    @SerializedName("ie_arpl_device_types")
    public List<String> ieArplDeviceTypes = List.of();

    @SerializedName("rl_gs_sg_v2_enabled")
    public boolean rlGsSgV2Enabled;

    @SerializedName("device_auth_magic_setup_enabled")
    public boolean deviceAuthMagicSetupEnabled;

    @SerializedName("ay_de_fk_enabled")
    public boolean ayDeFkEnabled;

    @SerializedName("nw_map_view_feature_enabled")
    public boolean nwMapViewFeatureEnabled;

    @SerializedName("hydrogen_enabled")
    public boolean hydrogenEnabled;

    @SerializedName("global_snooze_enabled")
    public boolean globalSnoozeEnabled;

    @SerializedName("ky_vr_enabled")
    public boolean kyVrEnabled;

    @SerializedName("fullscreen_scanning_enabled")
    public boolean fullscreenScanningEnabled;

    @SerializedName("ad_go_dy_ky_ms_ff2_enabled")
    public boolean adGoDyKyMsFf2Enabled;

    @SerializedName("ky_dy_te_enabled")
    public boolean kyDyTeEnabled;

    @SerializedName("ring_cam_enabled")
    public boolean ringCamEnabled;

    @SerializedName("wc_pk_cs_ehi_enabled")
    public boolean wcPkCsEhiEnabled;

    @SerializedName("audio_jitter_buffer_ms")
    public int audioJitterBufferMs;

    @SerializedName("rn_dd_oe_er_enabled")
    public boolean rnDdOeErEnabled;

    @SerializedName("vl_pd_mg_ss_es_enabled")
    public boolean vlPdMgSsEsEnabled;

    @SerializedName("zs_cg_sn_cy_enabled")
    public boolean zsCgSnCyEnabled;

    @SerializedName("hc_sg_device_types")
    public List<Object> hcSgDeviceTypes = List.of();

    @SerializedName("ota_timer_enabled")
    public boolean otaTimerEnabled;

    @SerializedName("sa_cd_cr_enabled")
    public boolean saCdCrEnabled;

    @SerializedName("sr_es_enabled")
    public boolean srEsEnabled;

    @SerializedName("se_cd_pk_enabled")
    public boolean seCdPkEnabled;

    @SerializedName("rl_qk_cl_enabled")
    public boolean rlQkClEnabled;

    @SerializedName("me_vs_le_vw_enabled")
    public boolean meVsLeVwEnabled;

    @SerializedName("p_s_v3_device_types")
    public List<String> pSV3DeviceTypes = List.of();

    @SerializedName("scallop_v3_enabled")
    public boolean scallopV3Enabled;

    @SerializedName("setup_shared_users_tutorial_v2_enabled")
    public boolean setupSharedUsersTutorialV2Enabled;

    @SerializedName("ss_ts_fr_am_lh_enabled")
    public boolean ssTsFrAmLhEnabled;

    @SerializedName("vg_wo_am_enabled")
    public boolean vgWoAmEnabled;

    @SerializedName("stickupcam_battery_setup_enabled")
    public boolean stickupcamBatterySetupEnabled;

    @SerializedName("rl_se_be_oa_enabled")
    public boolean rlSeBeOaEnabled;

    @SerializedName("rs.devicecatalog.allowlist.device.kwikset700HC918")
    public boolean rsDevicecatalogAllowlistDeviceKwikset700HC918;

    @SerializedName("zs_vs_enabled")
    public boolean zsVsEnabled;

    @SerializedName("scallop_lite_v2_enabled")
    public boolean scallopLiteV2Enabled;

    @SerializedName("timeline_adopt_p2_enabled")
    public boolean timelineAdoptP2Enabled;

    @SerializedName("rp_ce_as_enabled")
    public boolean rpCeAsEnabled;

    @SerializedName("ap_jn_ds2_enabled")
    public boolean apJnDs2Enabled;

    @SerializedName("gp_sg_v4_enabled")
    public boolean gpSgV4Enabled;

    @SerializedName("night_vision_enabled")
    public boolean nightVisionEnabled;

    @SerializedName("chime_pro_bt_setup_enabled")
    public boolean chimeProBtSetupEnabled;

    @SerializedName("ce_po_ms_enabled")
    public boolean cePoMsEnabled;

    @SerializedName("nsh_enabled")
    public boolean nshEnabled;

    @SerializedName("vg_sr_bn_enabled")
    public boolean vgSrBnEnabled;

    @SerializedName("ds_in_ds_enabled")
    public boolean dsInDsEnabled;

    @SerializedName("or_ct_sr_enabled")
    public boolean orCtSrEnabled;

    @SerializedName("mdlr_flc")
    public boolean mdlrFlc;

    @SerializedName("cy_ws_nw_enabled")
    public boolean cyWsNwEnabled;

    @SerializedName("sw_pn_dg_enabled")
    public boolean swPnDgEnabled;

    @SerializedName("remote_logging_level")
    public int remoteLoggingLevel;

    @SerializedName("history_updates_daterange_enabled")
    public boolean historyUpdatesDaterangeEnabled;

    @SerializedName("new_timeline_layout")
    public boolean newTimelineLayout;

    @SerializedName("guest_user_scheduling_enabled")
    public boolean guestUserSchedulingEnabled;

    @SerializedName("asu_enabled")
    public boolean asuEnabled;

    @SerializedName("ay_et_enabled")
    public boolean ayEtEnabled;

    @SerializedName("people_only_mode_device_types")
    public List<String> peopleOnlyModeDeviceTypes = List.of();

    @SerializedName("connectivity_wizard_enabled")
    public boolean connectivityWizardEnabled;

    @SerializedName("am_sl_lg_pe_rh_enabled")
    public boolean amSlLgPeRhEnabled;

    @SerializedName("pc_bn_enabled")
    public boolean pcBnEnabled;

    @SerializedName("sp_cm_enabled")
    public boolean spCmEnabled;

    @SerializedName("e2_su_ca_sp_enabled")
    public boolean e2SuCaSpEnabled;

    @SerializedName("cfes_eligible")
    public boolean cfesEligible;

    @SerializedName("ln_be_sp_enabled")
    public boolean lnBeSpEnabled;

    @SerializedName("rl_gs_sg_enabled")
    public boolean rlGsSgEnabled;

    @SerializedName("mdlr_stck")
    public boolean mdlrStck;

    @SerializedName("sl_of_enabled")
    public boolean slOfEnabled;

    @SerializedName("ls_v2_e_enabled")
    public boolean lsV2EEnabled;

    @SerializedName("test-bug-bash-gradual-rollout-firouzb-boolean")
    public boolean testBugBashGradualRolloutFirouzbBoolean;

    @SerializedName("le_vw_2_enabled")
    public boolean leVw2Enabled;

    @SerializedName("wc_nk_ur_enabled")
    public boolean wcNkUrEnabled;

    @SerializedName("sp_me_v_mn_v3_device_types")
    public List<String> spMeVMnV3DeviceTypes = List.of();

    @SerializedName("zs_pe_rn_enabled")
    public boolean zsPeRnEnabled;

    @SerializedName("aa_rs_enabled")
    public boolean aaRsEnabled;

    @SerializedName("ky_2_rg_dy_enabled")
    public boolean ky2RgDyEnabled;

    @SerializedName("ky_at_cs_enabled")
    public boolean kyAtCsEnabled;

    @SerializedName("mu_lo_sp_enabled")
    public boolean muLoSpEnabled;

    @SerializedName("py_bb_enabled")
    public boolean pyBbEnabled;

    @SerializedName("ap_rw_pt_enabled")
    public boolean apRwPtEnabled;

    @SerializedName("me_ss_dv_enabled")
    public boolean meSsDvEnabled;

    @SerializedName("pt_sp_st_cp_enabled")
    public boolean ptSpStCpEnabled;

    @SerializedName("e2_su_ca_ge_enabled")
    public boolean e2SuCaGeEnabled;

    @SerializedName("sh_enabled")
    public boolean shEnabled;

    @SerializedName("rs.devicecatalog.whitelist.device.retailkwickset500")
    public boolean rsDevicecatalogWhitelistDeviceRetailkwickset500;

    @SerializedName("dashboard_mon_sc")
    public boolean dashboardMonSc;

    @SerializedName("object_bounding_box_enabled")
    public boolean objectBoundingBoxEnabled;

    @SerializedName("nh_referral_v2_enabled")
    public boolean nhReferralV2Enabled;

    @SerializedName("mt_enabled")
    public boolean mtEnabled;

    @SerializedName("ky_vr_se_enabled")
    public boolean kyVrSeEnabled;

    @SerializedName("beams_gp_lt_sch_enabled")
    public boolean beamsGpLtSchEnabled;

    @SerializedName("pn_pg_on_enabled")
    public boolean pnPgOnEnabled;

    @SerializedName("police_portal_video_requests_enabled")
    public boolean policePortalVideoRequestsEnabled;

    @SerializedName("nw_user_activated")
    public boolean nwUserActivated;

    @SerializedName("pe_dn_device_types")
    public List<String> peDnDeviceTypes = List.of();

    @SerializedName("adaptive_video_play_enabled")
    public boolean adaptiveVideoPlayEnabled;

    @SerializedName("ch_auto_detection_enabled")
    public boolean chAutoDetectionEnabled;

    @SerializedName("cfes_enrolled")
    public boolean cfesEnrolled;

    @SerializedName("ambient_light_sensor_dpd_enabled")
    public boolean ambientLightSensorDpdEnabled;

    @SerializedName("alarm_language_preference_enabled")
    public boolean alarmLanguagePreferenceEnabled;

    @SerializedName("srtp_enabled")
    public boolean srtpEnabled;

    @SerializedName("video_resolutions_lpd_enabled")
    public boolean videoResolutionsLpdEnabled;

    @SerializedName("ring_beams_transformer_enabled")
    public boolean ringBeamsTransformerEnabled;

    @SerializedName("cd_bd_sa_1b_enabled")
    public boolean cdBdSa1bEnabled;

    @SerializedName("2v_pe_3_enabled")
    public boolean jsonMember2vPe3Enabled;

    @SerializedName("fw_wm_c2_enabled")
    public boolean fwWmC2Enabled;

    @SerializedName("cv_tr_device_types")
    public List<Object> cvTrDeviceTypes = List.of();

    @SerializedName("state_of_the_home_enabled")
    public boolean stateOfTheHomeEnabled;

    @SerializedName("ss_fr_ee_enabled")
    public boolean ssFrEeEnabled;

    @SerializedName("ringplus_all_partners_enabled")
    public boolean ringplusAllPartnersEnabled;

    @SerializedName("starred_events_enabled")
    public boolean starredEventsEnabled;

    @SerializedName("vo_vn_am_2_enabled")
    public boolean voVnAm2Enabled;

    @SerializedName("wc_re_mx_sk_rt_pr_sd_number")
    public int wcReMxSkRtPrSdNumber;

    @SerializedName("vf_tr_b")
    public boolean vfTrB;

    @SerializedName("bypass_account_verification")
    public boolean bypassAccountVerification;

    @SerializedName("re_bd_as_tl_enabled")
    public boolean reBdAsTlEnabled;

    @SerializedName("vf_tr_a")
    public boolean vfTrA;

    @SerializedName("amd_plg_zns_device_types")
    public List<String> amdPlgZnsDeviceTypes = List.of();

    @SerializedName("rs.backuprestore.includelist.kmjsupport")
    public boolean rsBackuprestoreIncludelistKmjsupport;

    @SerializedName("subscriptions_enabled")
    public boolean subscriptionsEnabled;

    @SerializedName("fe_am_an_ps_es_enabled")
    public boolean feAmAnPsEsEnabled;

    @SerializedName("motions_enabled")
    public boolean motionsEnabled;

    @SerializedName("night_mode_orion")
    public boolean nightModeOrion;

    @SerializedName("rp_to_enabled")
    public boolean rpToEnabled;

    @SerializedName("duress_alarm_test_enabled")
    public boolean duressAlarmTestEnabled;

    @SerializedName("e2_su_ca_br_enabled")
    public boolean e2SuCaBrEnabled;

    @SerializedName("chime_dnd_enabled")
    public boolean chimeDndEnabled;

    @SerializedName("nh_cy_enabled")
    public boolean nhCyEnabled;

    @SerializedName("kp_le_co_enabled")
    public boolean kpLeCoEnabled;

    @SerializedName("wk_wb_vw_ad_enabled")
    public boolean wkWbVwAdEnabled;

    @SerializedName("2fa_dur_reg_enabled")
    public boolean jsonMember2faDurRegEnabled;

    @SerializedName("rg_tl_enabled")
    public boolean rgTlEnabled;

    @SerializedName("ring_protect_enabled")
    public boolean ringProtectEnabled;

    @SerializedName("aa_bm_nv_p1_enabled")
    public boolean aaBmNvP1Enabled;

    @SerializedName("qa_manual_testing")
    public String qaManualTesting = "";

    @SerializedName("be_jy_ln_enabled")
    public boolean beJyLnEnabled;

    @SerializedName("et_hy_20_enabled")
    public boolean etHy20Enabled;

    @SerializedName("us_py_fs_pt_sp_device_types")
    public List<String> usPyFsPtSpDeviceTypes = List.of();

    @SerializedName("con_cen_enabled")
    public boolean conCenEnabled;

    @SerializedName("ml_dvc_enc_enrl_enabled")
    public boolean mlDvcEncEnrlEnabled;

    @SerializedName("fl_wtr_vlv_enabled")
    public boolean flWtrVlvEnabled;

    @SerializedName("vo_ds_10_enabled")
    public boolean voDs10Enabled;

    @SerializedName("mdlr_lnr")
    public boolean mdlrLnr;

    @SerializedName("he_st_as_pt_sp_enabled")
    public boolean heStAsPtSpEnabled;

    @SerializedName("emp_enabled")
    public boolean empEnabled;

    @SerializedName("sr_by_s_enabled")
    public boolean srBySEnabled;

    @SerializedName("rs.devicecatalog.whitelist.device.zcombo500")
    public boolean rsDevicecatalogWhitelistDeviceZcombo500;

    @SerializedName("a2a_lkg_enabled")
    public boolean a2aLkgEnabled;

    @SerializedName("sn_pr_v2_enabled")
    public boolean snPrV2Enabled;

    @SerializedName("ring_beams_wired_light_schedule_controls")
    public boolean ringBeamsWiredLightScheduleControls;

    @SerializedName("de_pe_ps_enabled")
    public boolean dePePsEnabled;

    @SerializedName("arpl_device_types")
    public List<String> arplDeviceTypes = List.of();

    @SerializedName("ss_sp_ff2_enabled")
    public boolean ssSpFf2Enabled;

    @SerializedName("mdlr_drb_pro_38")
    public boolean mdlrDrbPro38;

    @SerializedName("cocoa_pwr_mds_enabled")
    public boolean cocoaPwrMdsEnabled;

    @SerializedName("mdlr_drb_pro_35")
    public boolean mdlrDrbPro35;

    @SerializedName("ky_dy_te_lm_enabled")
    public boolean kyDyTeLmEnabled;

    @SerializedName("qr_ce_sr_enabled")
    public boolean qrCeSrEnabled;

    @SerializedName("rn_es_to_rp_enabled")
    public boolean rnEsToRpEnabled;

    @SerializedName("eml_vrf_p2_enabled")
    public boolean emlVrfP2Enabled;

    @SerializedName("be_jy_no_cp_at_nt_enabled")
    public boolean beJyNoCpAtNtEnabled;

    @SerializedName("doorbell_scallop_enabled")
    public boolean doorbellScallopEnabled;

    @SerializedName("power_cable_enabled")
    public boolean powerCableEnabled;

    @SerializedName("ff_24x7_lite_enabled")
    public boolean ff24x7LiteEnabled;

    @SerializedName("sr_lt_on_as_enabled")
    public boolean srLtOnAsEnabled;

    @SerializedName("gc_tw_device_types")
    public List<Object> gcTwDeviceTypes = List.of();

    @SerializedName("pe_wg_ge_enabled")
    public boolean peWgGeEnabled;

    @SerializedName("motion_detection_toggle_enabled")
    public boolean motionDetectionToggleEnabled;

    @SerializedName("eml_chng_enabled")
    public boolean emlChngEnabled;

    @SerializedName("bz_enabled")
    public boolean bzEnabled;

    @SerializedName("on_dr_ag_2_enabled")
    public boolean onDrAg2Enabled;

    @SerializedName("hk_enabled")
    public boolean hkEnabled;

    @SerializedName("et_hy_nw2_enabled")
    public boolean etHyNw2Enabled;

    @SerializedName("te_bd_wc_fe_pk_ff2_enabled")
    public boolean teBdWcFePkFf2Enabled;

    @SerializedName("duress_alarm_enabled")
    public boolean duressAlarmEnabled;

    @SerializedName("am_sl_fe_pe_me_enabled")
    public boolean amSlFePeMeEnabled;

    @SerializedName("finik_device_types")
    public List<String> finikDeviceTypes = List.of();

    @SerializedName("cr_nt_me_device_types")
    public List<String> crNtMeDeviceTypes = List.of();

    @SerializedName("sos_medical_enabled")
    public boolean sosMedicalEnabled;

    @SerializedName("rs.devicecatalog.whitelist.device.outdoorsiren")
    public boolean rsDevicecatalogWhitelistDeviceOutdoorsiren;

    @SerializedName("detailed_offline_messaging_enabled")
    public boolean detailedOfflineMessagingEnabled;

    @SerializedName("two_factor_auth_enabled")
    public boolean twoFactorAuthEnabled;

    @SerializedName("we_rc_io_device_types")
    public List<String> weRcIoDeviceTypes = List.of();

    @SerializedName("sw_oo_nf_enabled")
    public boolean swOoNfEnabled;

    @SerializedName("dy_dt_enabled")
    public boolean dyDtEnabled;

    @SerializedName("ld_lt_se_enabled")
    public boolean ldLtSeEnabled;

    @SerializedName("ld_ss_ce_bc1_enabled")
    public boolean ldSsCeBc1Enabled;

    @SerializedName("show_24x7_lite")
    public boolean show24x7Lite;

    @SerializedName("cd_bd_sa_gr_enabled")
    public boolean cdBdSaGrEnabled;

    @SerializedName("ce_po_bd_cd_device_types")
    public List<String> cePoBdCdDeviceTypes = List.of();

    @SerializedName("bs_dl_ot_st_pg_enabled")
    public boolean bsDlOtStPgEnabled;

    @SerializedName("nw_st_se_enabled")
    public boolean nwStSeEnabled;

    @SerializedName("pt_lp_sd_enabled")
    public boolean ptLpSdEnabled;

    @SerializedName("ky_2_rg_1_3_enabled")
    public boolean ky2Rg13Enabled;

    @SerializedName("te_or_enabled")
    public boolean teOrEnabled;

    @SerializedName("mdlr_flc2")
    public boolean mdlrFlc2;

    @SerializedName("ga_sk_be_enabled")
    public boolean gaSkBeEnabled;

    @SerializedName("mdlr_sclp")
    public boolean mdlrSclp;

    @SerializedName("alice_device_types")
    public List<String> aliceDeviceTypes = List.of();

    @SerializedName("gr_enabled")
    public boolean grEnabled;

    @SerializedName("nw_feed_types_enabled")
    public boolean nwFeedTypesEnabled;

    @SerializedName("ring_cash_eligible")
    public boolean ringCashEligible;

    @SerializedName("un_ir_ab_enabled")
    public boolean unIrAbEnabled;

    @SerializedName("delete_all_enabled")
    public boolean deleteAllEnabled;

    @SerializedName("sync_vod_lpd_enabled")
    public boolean syncVodLpdEnabled;

    @SerializedName("recording_indicators_device_type_enabled")
    public List<Object> recordingIndicatorsDeviceTypeEnabled = List.of();

    @SerializedName("an_de_device_types")
    public List<Object> anDeDeviceTypes = List.of();

    @SerializedName("p_s_oa_ch_ct_enabled")
    public boolean pSOaChCtEnabled;

    @SerializedName("te_bd_wc_fe_pk_enabled")
    public boolean teBdWcFePkEnabled;

    @SerializedName("twenty_four_seven_lite_enabled")
    public boolean twentyFourSevenLiteEnabled;

    @SerializedName("aa_ay_tb_p3_enabled")
    public boolean aaAyTbP3Enabled;

    @SerializedName("it_sp_te_enabled")
    public boolean itSpTeEnabled;

    @SerializedName("lv_or_pt_enabled")
    public boolean lvOrPtEnabled;

    @SerializedName("pt_lp_ir_enabled")
    public boolean ptLpIrEnabled;

    @SerializedName("zs_cn_ps_enabled")
    public boolean zsCnPsEnabled;

    @SerializedName("od_gd_enabled")
    public boolean odGdEnabled;

    @SerializedName("no_st_enabled")
    public boolean noStEnabled;

    @SerializedName("default_snapshot_tile_dashboard_enabled")
    public boolean defaultSnapshotTileDashboardEnabled;

    @SerializedName("email_verification_enabled")
    public boolean emailVerificationEnabled;

    @SerializedName("timeline_default_enabled")
    public boolean timelineDefaultEnabled;

    @SerializedName("rs.devicecatalog.whitelist.device.schlage500")
    public boolean rsDevicecatalogWhitelistDeviceSchlage500;

    @SerializedName("rn_ap_m1_enabled")
    public boolean rnApM1Enabled;

    @SerializedName("pt_lp_w1_enabled")
    public boolean ptLpW1Enabled;

    @SerializedName("advanced_motion_zones_enabled")
    public boolean advancedMotionZonesEnabled;

    @SerializedName("ujet_enabled")
    public boolean ujetEnabled;

    @SerializedName("kj_sk_ot_in_enabled")
    public boolean kjSkOtInEnabled;

    @SerializedName("kj_lf_ne_enabled")
    public boolean kjLfNeEnabled;

    @SerializedName("ring_search_enabled")
    public boolean ringSearchEnabled;

    @SerializedName("sm_in_pn3_enabled")
    public boolean smInPn3Enabled;

    @SerializedName("gelato_enabled")
    public boolean gelatoEnabled;

    @SerializedName("mdlr_drb_hzl")
    public boolean mdlrDrbHzl;

    @SerializedName("rp_wt_pn_ce_enabled")
    public boolean rpWtPnCeEnabled;

    @SerializedName("webrtc_enabled")
    public boolean webrtcEnabled;

    @SerializedName("av_ce_bu_enabled")
    public boolean avCeBuEnabled;

    @SerializedName("pt_lp_we_2_enabled")
    public boolean ptLpWe2Enabled;

    @SerializedName("show_motion_recording_types")
    public boolean showMotionRecordingTypes;

    @SerializedName("rpp_subscriptions_enabled")
    public boolean rppSubscriptionsEnabled;

    @SerializedName("cd_cs_enabled")
    public boolean cdCsEnabled;

    @SerializedName("mq_gd_enabled")
    public boolean mqGdEnabled;

    @SerializedName("hw_ts_sp_rk_enabled")
    public boolean hwTsSpRkEnabled;

    @SerializedName("accelerated_alarm_enabled")
    public boolean acceleratedAlarmEnabled;

    @SerializedName("he_mn_3d_pt_sp_enabled")
    public boolean heMn3dPtSpEnabled;

    @SerializedName("alarm_cs_tmpbyp_enabled")
    public boolean alarmCsTmpbypEnabled;

    @SerializedName("cd_bd_sa_4b_enabled")
    public boolean cdBdSa4bEnabled;

    @SerializedName("cl_as_ff2_enabled")
    public boolean clAsFf2Enabled;

    @SerializedName("gs_rl_in_ls_enabled")
    public boolean gsRlInLsEnabled;

    @SerializedName("lp_fg_rk_enabled")
    public boolean lpFgRkEnabled;

    @SerializedName("ay_ul_2_enabled")
    public boolean ayUl2Enabled;

    @SerializedName("mdlr_drb_cc")
    public boolean mdlrDrbCc;

    @SerializedName("device_health_alerts_enabled")
    public boolean deviceHealthAlertsEnabled;

    @SerializedName("timeline_thumbnails_enabled")
    public boolean timelineThumbnailsEnabled;

    @SerializedName("bs_mx_sr_enabled")
    public boolean bsMxSrEnabled;

    @SerializedName("rs.devicecatalog.whitelist.device.camelback")
    public boolean rsDevicecatalogWhitelistDeviceCamelback;

    @SerializedName("websockets_enabled")
    public boolean websocketsEnabled;

    @SerializedName("mc_enabled")
    public boolean mcEnabled;

    @SerializedName("wc_pk_cs_eha_enabled")
    public boolean wcPkCsEhaEnabled;

    @SerializedName("qs_ms_enabled")
    public boolean qsMsEnabled;

    @SerializedName("an_eo_ds_enabled")
    public boolean anEoDsEnabled;

    @SerializedName("silence_alarm_siren_enabled")
    public boolean silenceAlarmSirenEnabled;

    @SerializedName("reactive_snoozing_enabled")
    public boolean reactiveSnoozingEnabled;

    @SerializedName("cs_st_wn_rd_enabled")
    public boolean csStWnRdEnabled;

    @SerializedName("fr_en_enabled")
    public boolean frEnEnabled;

    @SerializedName("nh_po_te_enabled")
    public boolean nhPoTeEnabled;

    @SerializedName("mdlr_orn_39")
    public boolean mdlrOrn39;

    @SerializedName("rn_ce_enabled")
    public boolean rnCeEnabled;

    @SerializedName("dd_mg_stt_enabled")
    public boolean ddMgSttEnabled;

    @SerializedName("bd_ds_enabled")
    public boolean bdDsEnabled;

    @SerializedName("se_al_device_types")
    public List<Object> seAlDeviceTypes = List.of();

    @SerializedName("sw_oc_sr_v2_enabled")
    public boolean swOcSrV2Enabled;

    @SerializedName("vo_qy_te_enabled")
    public boolean voQyTeEnabled;

    @SerializedName("ou_si_enabled")
    public boolean ouSiEnabled;

    @SerializedName("ap_bl_ug_cn_enabled")
    public boolean apBlUgCnEnabled;

    @SerializedName("js_mn_zn2_enabled")
    public boolean jsMnZn2Enabled;

    @SerializedName("bs_ho_20_device_types")
    public List<String> bsHo20DeviceTypes = List.of();

    @SerializedName("post_setup_v2_device_types")
    public List<Object> postSetupV2DeviceTypes = List.of();

    @SerializedName("py_cd_sy_enabled")
    public boolean pyCdSyEnabled;

    @SerializedName("wwr_amazon_onboarding_enabled")
    public boolean wwrAmazonOnboardingEnabled;

    @SerializedName("bruno_set_enabled")
    public boolean brunoSetEnabled;

    @SerializedName("vl_cx_enabled")
    public boolean vlCxEnabled;

    @SerializedName("rs.devicecatalog.allowlist.device.yale700")
    public boolean rsDevicecatalogAllowlistDeviceYale700;

    @SerializedName("audio_recording_toggle_enabled")
    public boolean audioRecordingToggleEnabled;

    @SerializedName("sound_alarm_siren_enabled")
    public boolean soundAlarmSirenEnabled;

    @SerializedName("gt_as_ky_v2_enabled")
    public boolean gtAsKyV2Enabled;

    @SerializedName("ga_uc_bs_enabled")
    public boolean gaUcBsEnabled;

    @SerializedName("pt_lp_mot_enabled")
    public boolean ptLpMotEnabled;

    @SerializedName("affogato_v3_enabled")
    public boolean affogatoV3Enabled;

    @SerializedName("record_motion_enabled")
    public boolean recordMotionEnabled;

    @SerializedName("vrl31_dpd_enabled")
    public boolean vrl31DpdEnabled;

    @SerializedName("mdlr_orn_41")
    public boolean mdlrOrn41;

    @SerializedName("gc_sp_lv_enabled")
    public boolean gcSpLvEnabled;

    @SerializedName("de_sn_enabled")
    public boolean deSnEnabled;

    @SerializedName("st_enabled")
    public boolean stEnabled;

    @SerializedName("cd_bd_sa_sh_enabled")
    public boolean cdBdSaShEnabled;
}
