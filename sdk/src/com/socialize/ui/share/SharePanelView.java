/*
 * Copyright (c) 2011 Socialize Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.socialize.ui.share;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.socialize.ShareUtils;
import com.socialize.android.ioc.IBeanFactory;
import com.socialize.api.SocializeSession;
import com.socialize.api.action.ShareType;
import com.socialize.auth.AuthProviderType;
import com.socialize.config.SocializeConfig;
import com.socialize.entity.Entity;
import com.socialize.entity.Share;
import com.socialize.error.SocializeException;
import com.socialize.listener.SocializeAuthListener;
import com.socialize.listener.share.ShareAddListener;
import com.socialize.networks.SocialNetwork;
import com.socialize.networks.SocialNetworkListener;
import com.socialize.networks.facebook.FacebookSignInCell;
import com.socialize.networks.twitter.TwitterSignInCell;
import com.socialize.ui.auth.AnonymousCell;
import com.socialize.ui.dialog.SafeProgressDialog;
import com.socialize.ui.util.Colors;
import com.socialize.ui.view.ClickableSectionCell;
import com.socialize.ui.view.SocializeButton;
import com.socialize.util.DisplayUtils;
import com.socialize.util.Drawables;
import com.socialize.view.BaseView;

/**
 * @author Jason Polites
 */
public class SharePanelView extends BaseView {

	private ShareDialogListener listener;
	private SocialNetworkListener socialNetworkListener;
	private Dialog dialog;
	private SocializeConfig config;
	private Colors colors;
	
	private SocializeButton continueButton;
	
	private int displayOptions;
	
	private Entity entity;
	
	public SharePanelView(Context context, SocialNetworkListener socialNetworkListener, ShareDialogListener listener, Dialog dialog, int displayOptions) {
		this(context);
		this.listener = listener;
		this.socialNetworkListener = socialNetworkListener;
		this.dialog = dialog;
		this.displayOptions = displayOptions;
	}
	
	public SharePanelView(Context context, Entity entity, SocialNetworkListener socialNetworkListener, ShareDialogListener listener, Dialog dialog, int displayOptions) {
		this(context, socialNetworkListener, listener, dialog, displayOptions);
		this.entity = entity;
	}
	
	public SharePanelView(Context context, Entity entity, SocialNetworkListener socialNetworkListener, Dialog dialog, int displayOptions) {
		this(context, entity, socialNetworkListener, null, dialog, displayOptions);
	}
	
	public SharePanelView(Context context, Entity entity, Dialog dialog, int displayOptions) {
		this(context, entity, null, null, dialog, displayOptions);
	}
	
	public SharePanelView(Context context, Entity entity, ShareDialogListener listener, Dialog dialog, int displayOptions) {
		this(context, entity, null, listener, dialog, displayOptions);
	}
	
	public SharePanelView(Context context, ShareDialogListener listener, Dialog dialog, int displayOptions) {
		this(context);
		this.listener = listener;
		this.dialog = dialog;
		this.displayOptions = displayOptions;
	}
	
	public SharePanelView(Context context) {
		this(context, null, ShareUtils.SOCIAL);
	}
	
	public SharePanelView(Context context, Entity entity, int displayOptions) {
		super(context);
		this.displayOptions = displayOptions;
		this.entity = entity;
	}
	
	private IBeanFactory<FacebookSignInCell> facebookSignInCellFactory;
	private IBeanFactory<TwitterSignInCell> twitterSignInCellFactory;
	private IBeanFactory<EmailCell> emailCellFactory;
	private IBeanFactory<SMSCell> smsCellFactory;
	
	@SuppressWarnings("unused")
	private IBeanFactory<AnonymousCell> anonCellFactory; 
	
	private Drawables drawables;
	private DisplayUtils displayUtils;
	
	private FacebookSignInCell facebookSignInCell;
	private TwitterSignInCell twitterSignInCell;
	
	private EmailCell emailCell;
	private SMSCell smsCell;
	
	float radii = 6;
	int padding = 8;
	int headerHeight = 45;
	float headerRadius = 3;
	int landscapeButtonWidth = 190;
	
	private final float[] fbRadii = new float[]{radii, radii, radii, radii, 0.0f, 0.0f, 0.0f, 0.0f};
	private final int[] fbStroke = new int[]{1, 1, 0, 1};
	
	private final float[] twRadii = new float[]{0.0f, 0.0f, 0.0f, 0.0f, radii, radii, radii, radii};
	private final int[] twStroke = new int[]{1, 1, 1, 1};
	
	public void init() {
		
		boolean landscape = false;
		boolean lowRes = false;
		
		if(displayUtils != null) {
			padding = displayUtils.getDIP(12);
			headerRadius = displayUtils.getDIP(3);
			headerHeight = displayUtils.getDIP(45);
			radii = displayUtils.getDIP(8);
			landscape = displayUtils.isLandscape();
			lowRes = displayUtils.isLowRes();
			landscapeButtonWidth = displayUtils.getDIP(landscapeButtonWidth);
		}
		
		LayoutParams masterParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		masterParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
		
		setLayoutParams(masterParams);
		setOrientation(VERTICAL);
		
		RelativeLayout container = new RelativeLayout(getContext());
		LayoutParams containerParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		containerParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
		containerParams.weight = 1.0f;
		container.setLayoutParams(containerParams);
		
		makeShareButtons();
		
		View continueButtonLayout = makeContinueButton();
		View header = makeHeaderView(headerHeight, headerRadius);
		
		RelativeLayout.LayoutParams contentParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		contentParams.setMargins(padding, padding, padding, 0);
		contentParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		
		LinearLayout contentLayout = new LinearLayout(getContext());
		contentLayout.setPadding(padding, padding, padding, 0);
		contentLayout.setLayoutParams(contentParams);
		contentLayout.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
		
		LayoutParams socialNetworkButtonParams = null;
		LayoutParams emailSMSButtonParams = null;
		
		if(landscape) {
			socialNetworkButtonParams = new LayoutParams(landscapeButtonWidth, LayoutParams.WRAP_CONTENT);
			emailSMSButtonParams = new LayoutParams(landscapeButtonWidth, LayoutParams.WRAP_CONTENT);
			contentLayout.setOrientation(HORIZONTAL);
			
			socialNetworkButtonParams.setMargins(0, 0, padding/2, 0);
			emailSMSButtonParams.setMargins(padding/2, 0, 0, 0);
		}
		else {
			socialNetworkButtonParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			emailSMSButtonParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			emailSMSButtonParams.setMargins(0, padding, 0, 0);
			contentLayout.setOrientation(VERTICAL);
		}
		
		LinearLayout socialNetworkButtonLayout = new LinearLayout(getContext());
		socialNetworkButtonLayout.setPadding(0, 0, 0, 0);
		socialNetworkButtonLayout.setOrientation(VERTICAL);
		socialNetworkButtonLayout.setLayoutParams(socialNetworkButtonParams);	

		LinearLayout emailSMSButtonLayout = new LinearLayout(getContext());
		emailSMSButtonLayout.setPadding(0, 0, 0, 0);
		emailSMSButtonLayout.setOrientation(VERTICAL);
		emailSMSButtonLayout.setLayoutParams(emailSMSButtonParams);	
		
		if(!landscape && !lowRes) {
			View shareBadge = makeShareBadge();
			contentLayout.addView(shareBadge);
		}
		
		if(facebookSignInCell != null || twitterSignInCell != null) {
			if(facebookSignInCell != null) {
				socialNetworkButtonLayout.addView(facebookSignInCell);
			}
			if(twitterSignInCell != null) {
				socialNetworkButtonLayout.addView(twitterSignInCell);
			}
			contentLayout.addView(socialNetworkButtonLayout);
		}
		
		if(emailCell != null || smsCell != null) {
			if(emailCell != null) {
				emailSMSButtonLayout.addView(emailCell);
			}
			if(smsCell != null) {
				emailSMSButtonLayout.addView(smsCell);
			}
			contentLayout.addView(emailSMSButtonLayout);
		}		
		
		container.addView(contentLayout);
		
		addView(header);
		addView(container);
		addView(continueButtonLayout);
		
		toggleContinueButton();
		updateNetworkButtonState();
	}
	
	protected View makeContinueButton() {
		
		LinearLayout buttonLayout = new LinearLayout(getContext());
		
		if(continueButton != null) {

			LayoutParams buttonParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			
			buttonLayout.setPadding(padding, 0, padding, padding);
			buttonLayout.setOrientation(HORIZONTAL);
			buttonLayout.setLayoutParams(buttonParams);
			buttonLayout.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);		
			
			continueButton.setEnabled(false);
			continueButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					SocialNetwork[] networks = null;
					
					// Get the number of networks enabled
					if(facebookSignInCell != null && facebookSignInCell.isToggled()) {
						if(twitterSignInCell != null && twitterSignInCell.isToggled()) {
							networks = new SocialNetwork[]{SocialNetwork.FACEBOOK, SocialNetwork.TWITTER};
						}
						else {
							networks = new SocialNetwork[]{SocialNetwork.FACEBOOK};
						}
					}
					else if(twitterSignInCell != null && twitterSignInCell.isToggled()) {
						networks = new SocialNetwork[]{SocialNetwork.TWITTER};
					}
					
					listener.onContinue(dialog, networks);
				}
			});
			
			buttonLayout.addView(continueButton);
		}
		
		return buttonLayout;
	}
	
	protected View makeShareBadge() {
		RelativeLayout.LayoutParams badgeParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		LayoutParams badgeLayoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		badgeLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
		
		badgeParams.addRule(RelativeLayout.CENTER_IN_PARENT);		
		
		RelativeLayout badgeLayout = new RelativeLayout(getContext());
		badgeLayout.setLayoutParams(badgeLayoutParams);
		
		if(drawables != null) {
			ImageView authBadge = new ImageView(getContext());
			authBadge.setImageDrawable(drawables.getDrawable("share_badge.png"));
			authBadge.setLayoutParams(badgeParams);
			authBadge.setPadding(0, 0, 0, padding);
			badgeLayout.addView(authBadge);
		}
		
		return badgeLayout;
	}
	
	protected void makeShareButtons() {
		LayoutParams cellParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		boolean fbOK = getSocialize().isSupported(AuthProviderType.FACEBOOK) && ((displayOptions & ShareUtils.FACEBOOK) != 0) && facebookSignInCellFactory != null;
		boolean twOK = getSocialize().isSupported(AuthProviderType.TWITTER) && ((displayOptions & ShareUtils.TWITTER) != 0) && twitterSignInCellFactory != null;
		boolean emailOK = (entity != null && (displayOptions & ShareUtils.EMAIL) != 0) && getSocialize().canShare(getContext(), ShareType.EMAIL) && emailCellFactory != null;
		boolean smsOK = (entity != null && (displayOptions & ShareUtils.SMS) != 0) && getSocialize().canShare(getContext(), ShareType.SMS) && smsCellFactory != null;
		
		if(fbOK) {
			facebookSignInCell = facebookSignInCellFactory.getBean(this);
			
			if(facebookSignInCell != null) {
				facebookSignInCell.setLayoutParams(cellParams);
				facebookSignInCell.setPadding(padding, padding, padding, padding);
				
				if(twOK) {
					twitterSignInCell = twitterSignInCellFactory.getBean(this);
					twitterSignInCell.setPadding(padding, padding, padding, padding);
					twitterSignInCell.setLayoutParams(cellParams);
					
					facebookSignInCell.setBackgroundData(fbRadii, fbStroke, Color.BLACK);
					twitterSignInCell.setBackgroundData(twRadii, twStroke, Color.BLACK);
				}
			}
		}
		else if(twOK) {
			twitterSignInCell = twitterSignInCellFactory.getBean();
			
			if(twitterSignInCell != null) {
				twitterSignInCell.setLayoutParams(cellParams);
				twitterSignInCell.setPadding(padding, padding, padding, padding);
			}
		}
		
		if(emailOK) {
			emailCell = emailCellFactory.getBean();
			
			if(emailCell != null) {
				emailCell.setLayoutParams(cellParams);
				emailCell.setPadding(padding, padding, padding, padding);
				
				if(smsOK) {
					smsCell = smsCellFactory.getBean();
					smsCell.setLayoutParams(cellParams);
					smsCell.setPadding(padding, padding, padding, padding);

					emailCell.setBackgroundData(fbRadii, fbStroke, Color.BLACK);
					smsCell.setBackgroundData(twRadii, twStroke, Color.BLACK);
				}
			}
		}
		else if(smsOK) {
			smsCell = smsCellFactory.getBean();
			
			if(smsCell != null) {
				smsCell.setLayoutParams(cellParams);
				smsCell.setPadding(padding, padding, padding, padding);
			}
		}		
		
		if(facebookSignInCell != null) {
			facebookSignInCell.setAuthListener(getAuthClickListener(facebookSignInCell, SocialNetwork.FACEBOOK));
		}
		
		if(twitterSignInCell != null) {
			twitterSignInCell.setAuthListener(getAuthClickListener(twitterSignInCell, SocialNetwork.TWITTER));
		}
		
		if(emailCell != null) {
			emailCell.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					final ProgressDialog progress = SafeProgressDialog.show(v.getContext());
					ShareUtils.shareViaEmail(getActivity(), entity, new ShareAddListener() {
						
						@Override
						public void onError(SocializeException error) {
							progress.dismiss();
							showError(v.getContext(), error);
						}
						
						@Override
						public void onCreate(Share entity) {
							progress.dismiss();
						}
					});
				}
			});
		}
		
		if(smsCell != null) {
			smsCell.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					final ProgressDialog progress = SafeProgressDialog.show(v.getContext());
					ShareUtils.shareViaSMS(getActivity(), entity, new ShareAddListener() {
						
						@Override
						public void onError(SocializeException error) {
							progress.dismiss();
							showError(v.getContext(), error);
						}
						
						@Override
						public void onCreate(Share entity) {
							progress.dismiss();
						}
					});
				}
			});
		}		
	}
	
	protected View makeHeaderView(int headerHeight, float headerRadius) {
		LayoutParams headerParams = new LayoutParams(LayoutParams.FILL_PARENT, headerHeight);
		
		TextView header = new TextView(getContext());
		
		if(colors != null) {
			GradientDrawable headerBG = new GradientDrawable(Orientation.BOTTOM_TOP, new int[]{colors.getColor(Colors.AUTH_PANEL_BOTTOM), colors.getColor(Colors.AUTH_PANEL_TOP)});
			headerBG.setCornerRadii(new float[]{headerRadius, headerRadius, headerRadius, headerRadius, 0.0f, 0.0f, 0.0f, 0.0f});
			header.setBackgroundDrawable(headerBG);
		}

		header.setText("Share To...");
		header.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		header.setTextColor(Color.WHITE);
		header.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		header.setLayoutParams(headerParams);
		
		return header;
	}
	
	public void updateNetworkButtonState() {
		if(facebookSignInCell != null) {
			facebookSignInCell.setToggled(getSocialize().isAuthenticated(AuthProviderType.FACEBOOK) );
		}
		
		if(twitterSignInCell != null) {
			twitterSignInCell.setToggled(getSocialize().isAuthenticated(AuthProviderType.TWITTER));
		}
	}

	public void setFacebookSignInCellFactory(IBeanFactory<FacebookSignInCell> facebookSignInCellFactory) {
		this.facebookSignInCellFactory = facebookSignInCellFactory;
	}

	public void setTwitterSignInCellFactory(IBeanFactory<TwitterSignInCell> twitterSignInCellFactory) {
		this.twitterSignInCellFactory = twitterSignInCellFactory;
	}

	public void setDrawables(Drawables drawables) {
		this.drawables = drawables;
	}

	public void setDisplayUtils(DisplayUtils deviceUtils) {
		this.displayUtils = deviceUtils;
	}

	public FacebookSignInCell getFacebookSignInCell() {
		return facebookSignInCell;
	}

	public TwitterSignInCell getTwitterSignInCell() {
		return twitterSignInCell;
	}

	public void setAnonCellFactory(IBeanFactory<AnonymousCell> anonCellFactory) {
		this.anonCellFactory = anonCellFactory;
	}
	
	public void setConfig(SocializeConfig config) {
		this.config = config;
	}

	public void setColors(Colors colors) {
		this.colors = colors;
	}
	
	public void setContinueButton(SocializeButton continueButton) {
		this.continueButton = continueButton;
	}
	
//	public void setCancelButton(SocializeButton cancelButton) {
//		this.cancelButton = cancelButton;
//	}
	
	public void setEmailCellFactory(IBeanFactory<EmailCell> emailCellFactory) {
		this.emailCellFactory = emailCellFactory;
	}
	
	public void setSmsCellFactory(IBeanFactory<SMSCell> smsCellFactory) {
		this.smsCellFactory = smsCellFactory;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public void toggleContinueButton() {
		if(continueButton != null) {
			if(config.isAllowAnonymousUser()) {
				continueButton.setEnabled(true);
			}
			else {
				continueButton.setEnabled(twitterSignInCell.isToggled() || facebookSignInCell.isToggled());
			}
		}
	}

	protected SocializeAuthListener getAuthClickListener(final ClickableSectionCell cell, final SocialNetwork network) {
		return new SocializeAuthListener() {
			
			@Override
			public void onError(SocializeException error) {
				error.printStackTrace();
				
				showErrorToast(getContext(), error);
				
				if(socialNetworkListener != null) {
					socialNetworkListener.onError(getActivity(), network, error);
				}
			}
			
			@Override
			public void onAuthSuccess(SocializeSession session) {
				cell.setToggled(!cell.isToggled());
				toggleContinueButton();
				updateNetworkButtonState();
			}
			
			@Override
			public void onAuthFail(SocializeException error) {
				error.printStackTrace();
				
				showError(getContext(), error);
				
				if(socialNetworkListener != null) {
					socialNetworkListener.onError(getActivity(), network, error);
				}
			}

			@Override
			public void onCancel() {}
		};
	}	
}