package com.dhm47.nativeclipboard;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.dhm47.nativeclipboard.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;


@SuppressLint({ "InflateParams", "ClickableViewAccessibility" })
public class ClipBoard extends Service{
	private WindowManager windowManager;
	private ClipboardManager mClipboardManager;
	private LayoutInflater inflater;
	public static GridView gridView;
	private RelativeLayout mainLayout;
	private Context ctx;
	private Button clear;
	private ImageView close;
	private ClipAdapter clipAdapter;
	private SharedPreferences setting ;
	public static String backupS;
	public static int backupP;
	private int size;
	private View Undo;
	public static  List<String> pinned=new ArrayList<String>();
	private boolean clearall=false;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
    public void onCreate() {
        super.onCreate();
        }
	
	@SuppressWarnings("unchecked")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			windowManager.removeView(mainLayout);
		} catch (Exception e) {
			
		}
		ctx=getApplication().getApplicationContext();
		mClipboardManager =(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setting = ctx.getSharedPreferences("com.dhm47.nativeclipboard_preferences", 4);
		clipAdapter = new ClipAdapter(ctx);
		try {
			FileInputStream fisc = ctx.openFileInput("Clips");
			ObjectInputStream isc = new ObjectInputStream(fisc);
			ClipAdapter.mClips =  (List<String>) isc.readObject();
			size=ClipAdapter.mClips.size();
			isc.close();
			FileInputStream fisp = ctx.openFileInput("Pinned");
			ObjectInputStream isp = new ObjectInputStream(fisp);
			pinned =  (List<String>) isp.readObject();
			isp.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				Util.px(setting.getInt("windowsize",280), ctx),
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);
				params.gravity = Gravity.BOTTOM | Gravity.CENTER;
				params.windowAnimations=android.R.style.Animation_InputMethod;
				
		/*final DismissCallbacks mCallbacks=new SwipeDismissGridViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        if(pinned.contains(ClipAdapter.mClips.get(position)))return false;
                        else return true;
                    }

                    @Override
                    public void onDismiss(GridView gridView, int[] reverseSortedPositions) {
                       for (int position : reverseSortedPositions) {
                    	backupS=ClipAdapter.mClips.get(position);
        				backupP=position;
        				ClipAdapter.mClips.remove(position);}
                    	
        				clipAdapter.notifyDataSetChanged();
        				
        				try {windowManager.removeView(Undo);} catch (Exception e) {}
        				if(!clearall){//ClipAdapter.mClips.size()<size &&  && (ClipAdapter.mClips.size()!=0 || size==1)
        					Undo = inflater.inflate(R.layout.undo,null);
        					final CountDownTimer timeout=new CountDownTimer(3000, 3000) {
        			        	public void onTick(long millisUntilFinished) {}
        			            public void onFinish() {
        			            	try {windowManager.removeView(Undo);} catch (Exception e) {}
        			            }
        			        };
        			         
        				WindowManager.LayoutParams undoparams = new WindowManager.LayoutParams(
        						WindowManager.LayoutParams.WRAP_CONTENT,
        						WindowManager.LayoutParams.WRAP_CONTENT,
        						WindowManager.LayoutParams.TYPE_PHONE,
        						WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
        						PixelFormat.TRANSLUCENT);
        						undoparams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        						undoparams.y=Util.px(60, ctx);
        						undoparams.windowAnimations=android.R.style.Animation_Toast;
        				
        				TextView button =(TextView) Undo.findViewById(R.id.undobutton);
        				button.setOnClickListener(new OnClickListener() {
        					
        					@Override
        					public void onClick(View v) {
        						ClipAdapter.mClips.add(backupP, backupS);
        						timeout.cancel();
        						windowManager.removeView(Undo);
        						clipAdapter.notifyDataSetChanged();
        					}
        				});
        				TextView text =(TextView) Undo.findViewById(R.id.undotxt);
        				text.setText(getResources().getString(R.string.deleted)+backupS+" ");
        				windowManager.addView(Undo, undoparams);
        				timeout.start();
        				clearall=false;
        		      }
                    }
                };*/
        		
		mainLayout=(RelativeLayout) inflater.inflate(R.layout.clipboard,null);
		mainLayout.setOnTouchListener(new View.OnTouchListener() {
			@Override public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_OUTSIDE:
			mClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", ""));
			ClipBoard.this.stopSelf();
			return true;
			}
			return false;
			}
			});
		gridView =(GridView) mainLayout.findViewById(R.id.grid_view1);
		gridView.setAdapter(clipAdapter);
		clear= (Button) mainLayout.findViewById(R.id.clear);
		clear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog dialog ;
				AlertDialog.Builder confirm =new AlertDialog.Builder(ctx);
				confirm.setTitle(R.string.clear_all_conf);
				confirm.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						clearall=true;
						//touchListener.performDismiss(dismissView, dismissPosition);
						//mCallbacks.onDismiss(gridView, dismissPositions);
						ClipAdapter.mClips.clear();
						ClipAdapter.mClips.addAll(pinned);
						clipAdapter.notifyDataSetChanged();
						//windowManager.updateViewLayout(mainLayout, params);
						
					}
				});
				confirm.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						
					}
				});
				dialog = confirm.create();
				dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
				dialog.show();
							
			}
		});
		
		close= (ImageView) mainLayout.findViewById(R.id.close);
		close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", ""));
				ClipBoard.this.stopSelf();
				
				//for(int x=gridView.getChildCount()-1;x>0;x--){
				//gridView.getChildAt(x).animate().x(gridView.getChildAt(x-1).getX()).y(gridView.getChildAt(x-1).getY()).setDuration(3000).start();//}

			}
		});
		
		gridView.setBackgroundColor(setting.getInt("bgcolor",0x80E6E6E6));//setBackgroundResource(R.drawable.background);//gridView.setBackgroundColor(0x80E6E6E6);//setBackgroundColor(0xff141414);
		
		if(isColorDark(setting.getInt("bgcolor",0x80E6E6E6)))close.setImageResource(R.drawable.ic_close_dark);
		
		/*gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				mClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", ClipAdapter.mClips.get(position)));		
				ClipBoard.this.stopSelf();
				
			}
		});*/
		gridView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				final View mview = inflater.inflate(R.layout.bigtextview,null);
				mview.setBackgroundColor(setting.getInt("bgcolor",0x80E6E6E6));
				mview.setOnTouchListener(new View.OnTouchListener() {
					@Override public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_OUTSIDE:
					windowManager.removeView(mview);
					return true;
					}
					return false;
					}
					});
				
				final TextView textView =(TextView) mview.findViewById(R.id.textViewB);
				textView.setText(ClipAdapter.mClips.get(position));
				if(pinned.contains(ClipAdapter.mClips.get(position)))textView.setBackgroundColor(setting.getInt("pincolor",0xFFCF5300));
				else textView.setBackgroundColor(setting.getInt("clpcolor",0xFFFFBB22));
				textView.setTextColor(setting.getInt("txtcolor",0xffffffff));
				textView.setTextSize((float)(setting.getInt("txtsize",  20)));
				textView.setMovementMethod(new ScrollingMovementMethod());
				textView.setOnTouchListener(new OnTouchListener() {
				    float startx;
				    float starty;
					@Override
				    public boolean onTouch(View v, MotionEvent event) {
				        if(event.getAction() == MotionEvent.ACTION_UP && NotFar(event)){
				        	windowManager.removeView(mview);
							mClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", ClipAdapter.mClips.get(position)));		
				            ClipBoard.this.stopSelf();
				            return true;
				        }else if (event.getAction() == MotionEvent.ACTION_DOWN){
				        	startx=event.getRawX();
				        	starty=event.getRawY();}
				        	
				     
				        return false;
				    }
					private boolean NotFar(MotionEvent event){
						boolean returnVal = false;
						final int range = Util.px(15, ctx);
						if (Math.abs(startx - event.getRawX()) < range && Math.abs(starty - event.getRawY()) < range)
						returnVal = true;
						return returnVal;
						
					}
				});
				
				
				final ImageView delete =(ImageView) mview.findViewById(R.id.imageViewBD);
				delete.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						ClipAdapter.mClips.remove(position);
						clipAdapter.notifyDataSetChanged();
						windowManager.removeView(mview);
						windowManager.updateViewLayout(mainLayout, params);
						
					}
				});
				
				ImageView cancel =(ImageView) mview.findViewById(R.id.imageViewBC);
				cancel.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						windowManager.removeView(mview);
					}
				});
				ImageView pin=(ImageView) mview.findViewById(R.id.imageViewBS);
				pin.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(delete.getVisibility()==0){
							pinned.add(ClipAdapter.mClips.get(position));
							delete.setVisibility(View.INVISIBLE);
							textView.setBackgroundColor(setting.getInt("pincolor",0xFFCF5300));
							gridView.getChildAt(position-gridView.getFirstVisiblePosition()).setBackgroundColor(setting.getInt("pincolor",0xFFFF3300));
						}else{
							pinned.remove(ClipAdapter.mClips.get(position));
							delete.setVisibility(View.VISIBLE);
							textView.setBackgroundColor(setting.getInt("clpcolor",0xFFFFBB22));
							gridView.getChildAt(position-gridView.getFirstVisiblePosition()).setBackgroundColor(setting.getInt("clpcolor",0xFFFFBB22));
						}
					}
				});
				if(pinned.contains(ClipAdapter.mClips.get(position)))delete.setVisibility(View.INVISIBLE);
				windowManager.addView(mview, params);
				return true;
			}
		});

        //gridView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        //gridView.setOnScrollListener(touchListener.makeScrollListener());
		windowManager.addView(mainLayout, params);
		
		clipAdapter.registerDataSetObserver(new DataSetObserver() {
			public void onChanged() {
				try {windowManager.removeView(Undo);} catch (Exception e) {}
				if(ClipAdapter.mClips.size()<size && !clearall){//ClipAdapter.mClips.size()<size &&  && (ClipAdapter.mClips.size()!=0 || size==1)
					Undo = inflater.inflate(R.layout.undo,null);
					final CountDownTimer timeout=new CountDownTimer(3000, 3000) {
			        	public void onTick(long millisUntilFinished) {}
			            public void onFinish() {
			            	try {windowManager.removeView(Undo);} catch (Exception e) {}
			            	size=ClipAdapter.mClips.size();
			            }
			        };
			         
				WindowManager.LayoutParams undoparams = new WindowManager.LayoutParams(
						WindowManager.LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.TYPE_PHONE,
						WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
						PixelFormat.TRANSLUCENT);
						undoparams.gravity = Gravity.BOTTOM | Gravity.CENTER;
						undoparams.y=Util.px(60, ctx);
						undoparams.windowAnimations=android.R.style.Animation_Toast;
				
				TextView button =(TextView) Undo.findViewById(R.id.undobutton);
				button.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO add addition animation here
						timeout.cancel();
	        			windowManager.removeView(Undo);
	        			
	        				for(int x=(backupP-ClipBoard.gridView.getFirstVisiblePosition());x<ClipBoard.gridView.getLastVisiblePosition()-ClipBoard.gridView.getFirstVisiblePosition();x++){
	        					if(x<ClipBoard.gridView.getLastVisiblePosition()-ClipBoard.gridView.getFirstVisiblePosition()-1){
	        					ClipBoard.gridView.getChildAt(x).animate()
	        					.x(ClipBoard.gridView.getChildAt(x+1).getX())
	        					.y(ClipBoard.gridView.getChildAt(x+1).getY())
	        					.setDuration(ctx.getResources().getInteger(
	        			                android.R.integer.config_mediumAnimTime))
	        					.start();
	        					}else{
	        						ClipBoard.gridView.getChildAt(x).animate()
		        					.x(ClipBoard.gridView.getChildAt(x+1).getX())
		        					.y(ClipBoard.gridView.getChildAt(x+1).getY())
		        					.setDuration(ctx.getResources().getInteger(
		        			                android.R.integer.config_mediumAnimTime))
	        						.setListener(new AnimatorListenerAdapter() {
	                                    @Override
	                                    public void onAnimationEnd(Animator animation) {
	                                    	ClipAdapter.mClips.add(backupP, backupS);
	                                    	clipAdapter.notifyDataSetChanged();
	                                    	size=ClipAdapter.mClips.size();
	                                    }
	                                }).start();
	        					}}
	        			
												
					}
				});
				TextView text =(TextView) Undo.findViewById(R.id.undotxt);
				text.setText(getResources().getString(R.string.deleted)+backupS+" ");
				windowManager.addView(Undo, undoparams);
				timeout.start();
				clearall=false;
		      }
	
		    }
		});
		return START_STICKY;
	}
	@Override
	  public void onDestroy() {
		super.onDestroy();
		try {
			FileOutputStream fosc = ctx.openFileOutput("Clips", Context.MODE_PRIVATE);
			ObjectOutputStream osc = new ObjectOutputStream(fosc);
			osc.writeObject(ClipAdapter.mClips);
			osc.close();
			FileOutputStream fosp = ctx.openFileOutput("Pinned", Context.MODE_PRIVATE);
			ObjectOutputStream osp = new ObjectOutputStream(fosp);
			osp.writeObject(pinned);
			osp.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		if(mainLayout!=null)	windowManager.removeView(mainLayout);
		
	}

	public boolean isColorDark(int color){
	    double a = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
	    if(a<0.5){
	        return false; // It's a light color
	    }else{
	        return true; // It's a dark color
	    }
	}
	public static void animatedel (int position){
		
	}
	
}



//Animation
//Remove
//for(int x=gridView.getChildCount()-1;x>0;x--){
//gridView.getChildAt(x).animate().x(gridView.getChildAt(x-1).getX()).y(gridView.getChildAt(x-1).getY()).setDuration(3000).start();}
