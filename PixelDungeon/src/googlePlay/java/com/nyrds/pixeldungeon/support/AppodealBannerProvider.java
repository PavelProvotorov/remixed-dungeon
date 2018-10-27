package com.nyrds.pixeldungeon.support;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.BannerView;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;

class AppodealBannerProvider implements AdsUtilsCommon.IBannerProvider {

    private BannerView adView;

    @Override
    public void displayBanner() {
        AppodealRewardVideo.init(Appodeal.BANNER);

        Appodeal.cache(Game.instance(), Appodeal.BANNER);

        Appodeal.setBannerCallbacks(new AppodealBannerCallbacks());

        adView = Appodeal.getBannerView(Game.instance());

        if(!Appodeal.show(Game.instance(), Appodeal.BANNER_VIEW)){
            EventCollector.logEvent("banner", "appodeal_show_failed");
            AdsUtilsCommon.bannerFailed(AppodealBannerProvider.this);
        }
    }

    private class AppodealBannerCallbacks implements BannerCallbacks {
        @Override
        public void onBannerLoaded(int i, boolean b) {
            AdsUtils.updateBanner(adView);
        }

        @Override
        public void onBannerFailedToLoad() {
            EventCollector.logEvent("banner", "appodeal_no_banner");
            AdsUtilsCommon.bannerFailed(AppodealBannerProvider.this);
        }

        @Override
        public void onBannerShown() {

        }

        @Override
        public void onBannerClicked() {

        }

        @Override
        public void onBannerExpired() {

        }
    }
}
