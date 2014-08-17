/* 
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

var emptyFn = function() {};

module.exports = {

	/**
	 * This enum represents FlurryBanner's supported ad sizes.  Use one of these
	 * constants as the adSize when calling createBannerView.
	 * @const
	 */
	AD_SIZE: {
		BANNER: 'BANNER',
		SMART_BANNER: 'SMART_BANNER'
	},

	debugFlurry: function(enable) {
		cordova.exec(
			emptyFn,
			emptyFn,
			'FlurryBanner',
			'enableDebug',
			[enable !== false]
		);
	},

	setPublisherId: function(id) {
		cordova.exec(
			emptyFn,
			emptyFn,
			'FlurryBanner',
			'setPublisherId',
			[id]
		);
		cordova.exec(
			emptyFn,
			emptyFn,
			'FlurryInterstitial',
			'setPublisherId',
			[id]
		);
	},

	createBannerView: function(options) {
		options = options || {};
		cordova.exec(
			(options.success || emptyFn),
			(options.failure || emptyFn),
			'FlurryBanner',
			'createAdView',
			[options.bottom === false]
		);
	},

	createInterstitialView: function(options) {
		options = options || {};
		cordova.exec(
			(options.success || emptyFn),
			(options.failure || emptyFn),
			'FlurryInterstitial',
			'createAdView',
			[]
		);
	},

	destroyBannerView: function(options) {
		options = options || {};
		cordova.exec(
			(options.success || emptyFn),
			(options.failure || emptyFn),
			'FlurryBanner',
			'destroyAdView',
			[]
		);
	},

	requestBannerAd: function(options) {
		options = options || {};
		cordova.exec(
			(options.success || emptyFn),
			(options.failure || emptyFn),
			'FlurryBanner',
			'requestAd',
			[options.testing, options.showAdOnReceive]
		);
	},

	requestInterstitialAd: function(options) {
		options = options || {};
		cordova.exec(
			(options.success || emptyFn),
			(options.failure || emptyFn),
			'FlurryInterstitial',
			'requestAd',
			[options.testing, options.showAdOnReceive]
		);
	},

	showBannerAd: function(options) {
		options = options || {};
		cordova.exec(
			(options.success || emptyFn),
			(options.failure || emptyFn),
			'FlurryBanner',
			'showAd',
			[true]
		);
	},

	hideBannerAd: function(options) {
		options = options || {};
		cordova.exec(
			(options.success || emptyFn),
			(options.failure || emptyFn),
			'FlurryBanner',
			'showAd',
			[false]
		);
	}
};

