package org.almiso.collageapp.android.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import org.almiso.collageapp.android.R;
import org.almiso.collageapp.android.activity.StartActivity;
import org.almiso.collageapp.android.fragments.screens.RootController;
import org.almiso.collageapp.android.network.tasks.AsyncAction;
import org.almiso.collageapp.android.network.tasks.AsyncException;
import org.almiso.collageapp.android.network.tasks.CallBarrier;
import org.almiso.collageapp.android.network.tasks.CallbackHandler;
import org.almiso.collageapp.android.network.tasks.ProgressInterface;
import org.almiso.collageapp.android.network.tasks.RecoverCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * Created by almiso on 07.06.2014.
 */
public class CollageBaseFragment extends Fragment {

    protected static String TAG = "CollageBaseFragment";

    protected CollageApplication application;
    protected ActionBarActivity activity;
    private RootController rootController;
    private DisplayMetrics metrics;

    private static final ExecutorService service = Executors
            .newFixedThreadPool(5, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread res = new Thread(runnable);
                    res.setName("CollageBaseFragmentService#" + res.hashCode());
                    return res;
                }
            });


    private ProgressInterface dialogProgressInterfaceBackground = new ProgressInterface() {


        @Override
        public void showContent() {
        }

        @Override
        public void hideContent() {
        }


        @Override
        public void showProgress() {
        }

        @Override
        public void hideProgress() {
        }
    };

    private ProgressInterface dialogProgressInterface = new ProgressInterface() {

        private ProgressDialog progress;

        @Override
        public void showContent() {
        }

        @Override
        public void hideContent() {
        }


        @Override
        public void showProgress() {
            progress = ProgressDialog.show(activity,
                    getString(R.string.st_progress_title), getString(R.string.st_progress_message), true);
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
        }

        @Override
        public void hideProgress() {
            if (progress != null) {
                progress.dismiss();
                progress = null;
            }
        }
    };


    private ProgressInterface defaultProgressInterface = dialogProgressInterface;
    private CallbackHandler callbackHandler = new CallbackHandler() {
        @Override
        public void receiveCallback(final Runnable runnable) {
            handler.post(new Runnable() {
                @Override
                public String toString() {
                    return "handlerCallback";
                }

                @Override
                public void run() {
                    if (barrier.isPaused()) {
                        barrier.sendCallback(runnable);
                    } else {
                        runnable.run();
                    }
                }
            });
        }
    };

    private Handler handler = new Handler(Looper.getMainLooper());
    private CallBarrier barrier = new CallBarrier(callbackHandler, handler);

    public ProgressInterface getDefaultProgressInterface() {
        return defaultProgressInterface;
    }

    public ProgressInterface getBackgroundProgressInterface() {
        return dialogProgressInterfaceBackground;
    }

    public void setDefaultProgressInterface(
            ProgressInterface defaultProgressInterface) {
        this.defaultProgressInterface = defaultProgressInterface;
    }

    private RecoverCallback dialogRecoverCallback = new RecoverCallback() {
        @Override
        public void onError(AsyncException e, final Runnable onRepeat,
                            final Runnable onCancel) {
            String errorMessage = e.getMessage();
            if (e.getType() != null) {
                switch (e.getType()) {
                    case UNKNOWN_ERROR:
                        errorMessage = getString(R.string.st_error_unknown);
                        break;
                    case CONNECTION_ERROR:
                        errorMessage = getString(R.string.st_error_connection);
                        break;
                    case NO_USER_FOUND:
                        errorMessage = getString(R.string.st_error_no_user_found);
                        break;
                    case LOAD_ERROR:
                        errorMessage = getString(R.string.st_error_loading);
                        break;
                    case CUSTOM_ERROR:
                        errorMessage = e.getMessage();
                        break;
                    default:
                        errorMessage = e.getMessage();
                }
            }

            if (e.isRepeatable()) {

                AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.st_error_title)
                        .setMessage(errorMessage)
                        .setPositiveButton(R.string.st_repeat, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onRepeat.run();
                            }
                        }).setNegativeButton(R.string.st_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onCancel.run();
                            }
                        }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();


            } else {

                AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.st_error_title)
                        .setMessage(errorMessage)
                        .setPositiveButton(R.string.st_close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onCancel.run();
                            }
                        }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

            }
        }
    };

    public void runUiTask(final AsyncAction task) {
        asyncTask(task, dialogRecoverCallback, defaultProgressInterface);
    }

    public void runUiTask(final AsyncAction task,
                          RecoverCallback recoverCallback) {
        asyncTask(task, recoverCallback, defaultProgressInterface);
    }

    public void runUiTask(final AsyncAction task,
                          ProgressInterface progressInterface) {
        asyncTask(task, dialogRecoverCallback, progressInterface);
    }


    private void asyncTask(final AsyncAction action,
                           final RecoverCallback recoverCallback,
                           final ProgressInterface taskProgressInterface) {
        secureCallback(new Runnable() {

            @Override
            public String toString() {
                return "asyncTaskOuter";
            }

            @Override
            public void run() {
                if (taskProgressInterface != null) {
                    taskProgressInterface.hideContent();
                    taskProgressInterface.showProgress();
                }
                action.beforeExecute();
                service.submit(new Runnable() {

                    @Override
                    public String toString() {
                        return "asyncTaskTask";
                    }

                    @Override
                    public void run() {
                        try {
                            action.execute();
                            secureCallback(new Runnable() {

                                @Override
                                public String toString() {
                                    return "asyncTaskTask1";
                                }

                                @Override
                                public void run() {
                                    action.afterExecute();
                                    if (taskProgressInterface != null) {
                                        taskProgressInterface.showContent();
                                        taskProgressInterface.hideProgress();
                                    }
                                }
                            });
                        } catch (final Exception e) {
                            e.printStackTrace();
                            final AsyncException asyncException = e instanceof AsyncException ? (AsyncException) e
                                    : new AsyncException(
                                    AsyncException.ExceptionType.UNKNOWN_ERROR,
                                    e);

                            if (recoverCallback != null) {
                                secureCallback(new Runnable() {

                                    @Override
                                    public String toString() {
                                        return "asyncTaskTask2";
                                    }

                                    @Override
                                    public void run() {
                                        recoverCallback.onError(asyncException,
                                                new Runnable() {

                                                    @Override
                                                    public String toString() {
                                                        return "asyncTaskTask_error";
                                                    }

                                                    @Override
                                                    public void run() {
                                                        asyncTask(
                                                                action,
                                                                recoverCallback,
                                                                taskProgressInterface);
                                                    }
                                                }, new Runnable() {

                                                    @Override
                                                    public String toString() {
                                                        return "asyncTaskTask_cancel";
                                                    }

                                                    @Override
                                                    public void run() {
                                                        barrier.sendCallback(new Runnable() {

                                                            @Override
                                                            public String toString() {
                                                                return "asyncTaskTask_cancel_inner";
                                                            }

                                                            @Override
                                                            public void run() {
                                                                action.onCanceled();
                                                            }
                                                        });
                                                    }
                                                }
                                        );
                                        if (taskProgressInterface != null) {
                                            taskProgressInterface
                                                    .hideProgress();
                                        }
                                    }
                                });
                            } else {
                                secureCallback(new Runnable() {
                                    @Override
                                    public String toString() {
                                        return "asyncTaskTask_ex";
                                    }

                                    @Override
                                    public void run() {
                                        action.onException(asyncException);
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    public void secureCallback(Runnable runnable) {
        barrier.sendCallback(runnable);
    }

    /**
     * ***************************************************
     */

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        barrier.pause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ensureApp();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ensureApp();
        activity = (ActionBarActivity) getActivity();
        metrics = getResources().getDisplayMetrics();
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        ensureApp();
        TAG = ((Object) this).getClass().getSimpleName();
        barrier.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barrier.pause();
    }


    /**
     * ***************************************************
     */

    private void ensureApp() {
        if (application == null) {
            application = (CollageApplication) getActivity().getApplicationContext();
        }
        rootController = null;
        if (getActivity() instanceof StartActivity) {
            rootController = ((StartActivity) getActivity()).getRootController();
        }
    }

    protected RootController getRootController() {
        return rootController;
    }


    protected int getPx(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                metrics);
    }

    protected int getSp(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                metrics);
    }


}
