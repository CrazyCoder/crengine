package org.coolreader.crengine;

import java.util.ArrayList;

import org.coolreader.R;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

public class CRToolBar extends ViewGroup {


	private static final Logger log = L.create("tb");
	
	final private BaseActivity activity;
	private ArrayList<ReaderAction> actions = new ArrayList<ReaderAction>();
	private ArrayList<ReaderAction> iconActions = new ArrayList<ReaderAction>();
	private boolean showLabels;
	private int buttonHeight;
	private int buttonWidth;
	private int itemHeight; // multiline mode, line height 
	private int visibleButtonCount;
	private int visibleNonButtonCount;
	private boolean isVertical;
	private boolean isMultiline;
	final private int preferredItemHeight;
	private int BUTTON_SPACING = 4;
	private int BAR_SPACING = 4;
	private int buttonAlpha = 0xFF;
	private int textColor = 0x000000;
	private ImageButton overflowButton;
	private LayoutInflater inflater;

	private ArrayList<ReaderAction> itemsOverflow = new ArrayList<ReaderAction>();
	
	public void setButtonAlpha(int alpha) {
		this.buttonAlpha = alpha;
		if (isShown()) {
			requestLayout();
			invalidate();
		}
	}
	
	public void setVertical(boolean vertical) {
		this.isVertical = vertical;
		if (isVertical) {
			//setPadding(BUTTON_SPACING, BUTTON_SPACING, BAR_SPACING, BUTTON_SPACING);
			setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
		} else {
			//setPadding(BUTTON_SPACING, BAR_SPACING, BUTTON_SPACING, BUTTON_SPACING);
			setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}
	}
	public boolean isVertical() {
		return this.isVertical;
	}

	public CRToolBar(BaseActivity context) {
		super(context);
		this.activity = context;
		this.preferredItemHeight = context.getPreferredItemHeight();
	}

	private LinearLayout inflateItem(ReaderAction action) {
		final LinearLayout view = (LinearLayout)inflater.inflate(R.layout.popup_toolbar_item, null);
		ImageView icon = (ImageView)view.findViewById(R.id.action_icon);
		TextView label = (TextView)view.findViewById(R.id.action_label);
		icon.setImageResource(action != null ? action.iconId : R.drawable.cr3_button_more);
		//icon.setMinimumHeight(buttonHeight);
		icon.setMinimumWidth(buttonWidth);
		Utils.setContentDescription(icon, activity.getString(action != null ? action.nameId : R.string.btn_toolbar_more));
		label.setText(action != null ? action.nameId : R.string.btn_toolbar_more);
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		return view;
	}
	
	public CRToolBar(BaseActivity context, ArrayList<ReaderAction> actions, boolean multiline) {
		super(context);
		this.activity = context;
		this.actions = actions;
		this.showLabels = multiline;
		this.isMultiline = multiline;
		this.preferredItemHeight = context.getPreferredItemHeight();
		this.inflater = LayoutInflater.from(activity);
		context.getWindow().getAttributes();
		if (context.isSmartphone()) {
			BUTTON_SPACING = 3;
			BAR_SPACING = 3;
		} else {
			BUTTON_SPACING = preferredItemHeight / 20;
			BAR_SPACING = preferredItemHeight / 20;
		}
		int sz = (context.isSmartphone() ? preferredItemHeight * 3 / 4 - BUTTON_SPACING : preferredItemHeight);
		buttonWidth = buttonHeight = sz;
		if (multiline)
			buttonHeight = sz / 2;
		int dpi = context.getDensityDpi();
		for (int i=0; i<actions.size(); i++) {
			ReaderAction item = actions.get(i);
			int iconId = item.iconId;
			if (iconId == 0) {
				itemsOverflow.add(item);
				visibleNonButtonCount++;
				continue;
			}
			iconActions.add(item);
			Drawable d = context.getResources().getDrawable(iconId);
			visibleButtonCount++;
			int w = d.getIntrinsicWidth() * dpi / 160 + 4;
			int h = d.getIntrinsicHeight() * dpi / 160 + 4;
			if (buttonWidth < w) {
				buttonWidth = w;
			}
			if (buttonHeight < h) {
				buttonHeight = h;
			}
		}
		if (multiline) {
			LinearLayout item = inflateItem(iconActions.get(0));
			itemHeight = item.getMeasuredHeight() + BUTTON_SPACING;
		}
	}

	private OnActionHandler onActionHandler;
	
	public void setOnActionHandler(OnActionHandler handler) {
		this.onActionHandler = handler;
	}
	
	private OnOverflowHandler onOverflowHandler;
	
	public void setOnOverflowHandler(OnOverflowHandler handler) {
		this.onOverflowHandler = handler;
	}
	
	public interface OnActionHandler {
		boolean onActionSelected(ReaderAction item);
	}
	public interface OnOverflowHandler {
		boolean onOverflowActions(ArrayList<ReaderAction> actions);
	}
	
	
	private ReaderAction findByCmd(int id) {
		for (ReaderAction action : actions)
			if (id == action.cmd.getNativeId())
				return action;
		return null;
	}
	
	@Override
	protected void onCreateContextMenu(ContextMenu menu) {
		int order = 0;
		for (ReaderAction action : itemsOverflow) {
			menu.add(0, action.menuItemId, order++, action.nameId);
		}
	}
	
	public void showOverflowMenu() {
		if (itemsOverflow.size() > 0) {
			if (onOverflowHandler != null)
				onOverflowHandler.onOverflowActions(itemsOverflow);
			else {
				if (!isMultiline && visibleNonButtonCount == 0)
					showPopup(activity, activity.getContentView(), actions, onActionHandler, onOverflowHandler);
				else
					activity.showActionsPopupMenu(itemsOverflow, onActionHandler);
			}
//			PopupMenu menu = new PopupMenu(activity, this);
//			int order = 0;
//			for (ReaderAction action : itemsOverflow) {
//				menu.getMenu().add(0, action.menuItemId, order++, action.nameId);
//			}
//			menu.show();
//			showContextMenuForChild(overflowButton);
		}
	}
	
//	private void onMoreButtonClick() {
//		showOverflowMenu();
//	}
	
	private void onButtonClick(ReaderAction item) {
		if (onActionHandler != null)
			onActionHandler.onActionSelected(item);
	}
	
	private ImageButton addButton(Rect rect, final ReaderAction item, boolean left) {
		Rect rc = new Rect(rect);
		if (isVertical) {
			if (left) {
				rc.bottom = rc.top + buttonHeight;
				rect.top += buttonHeight + BUTTON_SPACING;
			} else {
				rc.top = rc.bottom - buttonHeight;
				rect.bottom -= buttonHeight + BUTTON_SPACING;
			}
		} else {
			if (left) {
				rc.right = rc.left + buttonWidth;
				rect.left += buttonWidth + BUTTON_SPACING;
			} else {
				rc.left = rc.right - buttonWidth;
				rect.right -= buttonWidth + BUTTON_SPACING;
			}
		}
		if (rc.isEmpty())
			return null;
		ImageButton ib = new ImageButton(getContext());
		if (item != null) {
			ib.setImageResource(item.iconId);
			Utils.setContentDescription(ib, getContext().getString(item.nameId));
			ib.setTag(item);
		} else {
			ib.setImageDrawable(getResources().getDrawable(R.drawable.cr3_button_more));
			Utils.setContentDescription(ib, getContext().getString(R.string.btn_toolbar_more));
		}
		ib.setBackgroundResource(R.drawable.cr3_toolbar_button_background);
		ib.layout(rc.left, rc.top, rc.right, rc.bottom);
		if (item == null)
			overflowButton = ib;
		ib.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (item != null)
					onButtonClick(item);
				else
					showOverflowMenu();
			}
		});
		ib.setAlpha(nightMode ? 0x60 : buttonAlpha);
		addView(ib);
		return ib;
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		right -= left;
		bottom -= top;
		left = top = 0;
		removeAllViews();
		overflowButton = null;
		
		if (isMultiline) {
        	int lineCount = calcLineCount(right);
        	int btnCount = iconActions.size() + (visibleNonButtonCount > 0 ? 1 : 0);
        	int buttonsPerLine = (btnCount + lineCount - 1) / lineCount;

//        	ScrollView scroll = new ScrollView(activity);
//        	scroll.setLayoutParams(new LayoutParams(right, bottom));
//        	AbsoluteLayout content = new AbsoluteLayout(activity);
        	
        	
    		Rect rect = new Rect(left + getPaddingLeft() + BUTTON_SPACING, top + getPaddingTop() + BUTTON_SPACING, right - getPaddingRight() - BUTTON_SPACING, bottom - getPaddingBottom() - BUTTON_SPACING);
    		int lineH = itemHeight; //rect.height() / lineCount;
    		int spacing = 0;
        	for (int currentLine = 0; currentLine < lineCount; currentLine++) {
        		int startBtn = currentLine * buttonsPerLine;
        		int endBtn = (currentLine + 1) * buttonsPerLine;
        		if (endBtn > btnCount)
        			endBtn = btnCount;
        		int currentLineButtons = endBtn - startBtn;
        		Rect lineRect = new Rect(rect);
        		lineRect.top += currentLine * lineH + spacing;
        		lineRect.bottom = lineRect.top + lineH - spacing;
        		int itemWidth = lineRect.width() / currentLineButtons;
        		for (int i = 0; i < currentLineButtons; i++) {
        			Rect itemRect = new Rect(lineRect);
        			itemRect.left += i * itemWidth + spacing;
        			itemRect.right = itemRect.left + itemWidth - spacing;
        			final ReaderAction action = (visibleNonButtonCount > 0 && i + startBtn == iconActions.size()) ? null : iconActions.get(startBtn + i);
        			log.v("item=" + itemRect);
        			LinearLayout item = inflateItem(action);
        			//item.setLayoutParams(new LinearLayout.LayoutParams(itemRect.width(), itemRect.height()));
        			item.measure(MeasureSpec.makeMeasureSpec(itemRect.width(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(itemRect.height(), MeasureSpec.EXACTLY));
        			item.layout(itemRect.left, itemRect.top, itemRect.right, itemRect.bottom);
        			//item.forceLayout();
        			addView(item);
        			item.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (action != null)
								onButtonClick(action);
							else
								showOverflowMenu();
						}
					});
        		}
//        		addView(scroll);
        	}
        	return;
		}

//		View divider = new View(getContext());
//		addView(divider);
//		if (isVertical()) {
//			divider.setBackgroundResource(R.drawable.divider_light_vertical_tiled);
//			divider.layout(right - 8, top, right, bottom);
//		} else {
//			divider.setBackgroundResource(R.drawable.divider_light_tiled);
//			divider.layout(left, bottom - 8, right, bottom);
//		}

		visibleButtonCount = 0;
		for (int i=0; i<actions.size(); i++) {
			if (actions.get(i).iconId != 0)
				visibleButtonCount++;
		}
		
		
		Rect rect = new Rect(left + getPaddingLeft() + BUTTON_SPACING, top + getPaddingTop() + BUTTON_SPACING, right - getPaddingRight() - BUTTON_SPACING, bottom - getPaddingBottom() - BUTTON_SPACING);
		if (rect.isEmpty())
			return;
		ArrayList<ReaderAction> itemsToShow = new ArrayList<ReaderAction>();
		itemsOverflow.clear();
		int maxButtonCount = 1;
		if (isVertical) {
			rect.right -= BAR_SPACING;
			int maxHeight = bottom - top - getPaddingTop() - getPaddingBottom() + BUTTON_SPACING;
			maxButtonCount = maxHeight / (buttonHeight + BUTTON_SPACING);
		} else {
			rect.bottom -= BAR_SPACING;
			int maxWidth = right - left - getPaddingLeft() - getPaddingRight() + BUTTON_SPACING;
			maxButtonCount = maxWidth / (buttonWidth + BUTTON_SPACING);
		}
		int count = 0;
		boolean addEllipsis = visibleButtonCount > maxButtonCount || visibleNonButtonCount > 0;
		if (addEllipsis) {
			addButton(rect, null, false);
			maxButtonCount--;
		}
		for (int i = 0; i < actions.size(); i++) {
			ReaderAction item = actions.get(i);
			if (count >= maxButtonCount) {
				itemsOverflow.add(item);
				continue;
			}
			if (item.iconId == 0) {
				itemsOverflow.add(item);
				continue;
			}
			itemsToShow.add(item);
			count++;
			addButton(rect, item, true);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        if (widthMode != MeasureSpec.EXACTLY) {
//            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " +
//                    "with android:layout_width=\"match_parent\" (or fill_parent)");
//        }
        
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        if (heightMode != MeasureSpec.AT_MOST) {
//            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " +
//                    "with android:layout_height=\"wrap_content\"");
//        }

        if (isVertical) {
	        int contentHeight = MeasureSpec.getSize(heightMeasureSpec);
	        int maxWidth = buttonWidth + BUTTON_SPACING + BUTTON_SPACING + BAR_SPACING + getPaddingLeft() + getPaddingRight();
	        setMeasuredDimension(maxWidth, contentHeight);
        } else {
	        int contentWidth = MeasureSpec.getSize(widthMeasureSpec);
	        if (isMultiline) {
		        int contentHeight = MeasureSpec.getSize(heightMeasureSpec);
	        	int lineCount = calcLineCount(contentWidth);
	        	int h = lineCount * itemHeight + BAR_SPACING + BAR_SPACING;
//	        	if (h > contentHeight - itemHeight)
//	        		h = contentHeight - itemHeight;
	        	setMeasuredDimension(contentWidth, h);
	        } else {
	        	setMeasuredDimension(contentWidth, buttonHeight + BUTTON_SPACING * 2 + BAR_SPACING);
	        }
        }
	}
	
	protected int calcLineCount(int contentWidth) {
		if (!isMultiline)
			return 1;
    	int lineCount = 1;
    	int btnCount = iconActions.size() + (visibleNonButtonCount > 0 ? 1 : 0);
    	int minLineItemCount = 3;
    	int maxLineItemCount = contentWidth / (preferredItemHeight * 3);
    	if (maxLineItemCount < minLineItemCount)
    		maxLineItemCount = minLineItemCount;

    	for (;;) {
	    	if (btnCount <= maxLineItemCount * lineCount)
	    		return lineCount;
	    	lineCount++;
    	}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		log.v("CRToolBar.onSizeChanged(" + w + ", " + h + ")");
	}
	
	

	@Override
	protected void onDraw(Canvas canvas) {
		log.v("CRToolBar.onDraw(" + getWidth() + ", " + getHeight() + ")");
		super.onDraw(canvas);
	}
	public PopupWindow showAsPopup(View anchor, OnActionHandler onActionHandler, OnOverflowHandler onOverflowHandler) {
		return showPopup(activity, anchor, actions, onActionHandler, onOverflowHandler);
	}
	
	public static PopupWindow showPopup(BaseActivity context, View anchor, ArrayList<ReaderAction> actions, final OnActionHandler onActionHandler, final OnOverflowHandler onOverflowHandler) {
		final ScrollView scroll = new ScrollView(context);
		final CRToolBar tb = new CRToolBar(context, actions, true);
		tb.setOnActionHandler(onActionHandler);
		tb.setVertical(false);
		tb.measure(MeasureSpec.makeMeasureSpec(anchor.getWidth(), MeasureSpec.EXACTLY), ViewGroup.LayoutParams.WRAP_CONTENT);
		int w = tb.getMeasuredWidth();
		int h = tb.getMeasuredHeight();
		scroll.addView(tb);
		scroll.setLayoutParams(new LayoutParams(w, h/2));
		scroll.setVerticalFadingEdgeEnabled(true);
		scroll.setFadingEdgeLength(h / 10);
		final PopupWindow popup = new PopupWindow(context);
		popup.setTouchInterceptor(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction()==MotionEvent.ACTION_OUTSIDE ) {
					popup.dismiss();
					return true;
				}
				return false;
			}
		});
		tb.setOnActionHandler(new OnActionHandler() {
			@Override
			public boolean onActionSelected(ReaderAction item) {
				popup.dismiss();
				return onActionHandler.onActionSelected(item);
			}
		});
		tb.setOnOverflowHandler(new OnOverflowHandler() {
			@Override
			public boolean onOverflowActions(ArrayList<ReaderAction> actions) {
				popup.dismiss();
				return onOverflowHandler.onOverflowActions(actions);
			}
		});
		//popup.setBackgroundDrawable(new BitmapDrawable());
		popup.setWidth(WindowManager.LayoutParams.FILL_PARENT);
		int hh = h;
		int maxh = anchor.getHeight();
		if (hh > maxh - context.getPreferredItemHeight())
			hh = maxh - context.getPreferredItemHeight() * 3 / 2;
		popup.setHeight(hh);
		popup.setFocusable(true);
		popup.setFocusable(true);
		popup.setTouchable(true);
		popup.setOutsideTouchable(true);
		popup.setContentView(scroll);
		InterfaceTheme theme = context.getCurrentTheme();
		Drawable bg;
		if (theme.getPopupToolbarBackground() != 0)
			bg = context.getResources().getDrawable(theme.getPopupToolbarBackground());
		else
			bg = Utils.solidColorDrawable(theme.getPopupToolbarBackgroundColor());
		popup.setBackgroundDrawable(bg);
		int [] location = new int[2];
		anchor.getLocationOnScreen(location);
		int popupY = location[1];
		popup.showAtLocation(anchor, Gravity.TOP | Gravity.LEFT, location[0], popupY);
		return popup;
	}
	
	private boolean nightMode;
	public void updateNightMode(boolean nightMode) {
		if (this.nightMode != nightMode) {
			this.nightMode = nightMode;
			if (isShown()) {
				requestLayout();
				invalidate();
			}
		}
	}
	
	public void onThemeChanged(InterfaceTheme theme) {
		//buttonAlpha = theme.getToolbarButtonAlpha();
		//textColor = theme.getStatusTextColor();
		if (isShown()) {
			requestLayout();
			invalidate();
		}
	}
}
