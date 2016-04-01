package com.jobmineplus.mobile.activities.jbmnpls;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.SSLException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import com.jobmineplus.mobile.R;
import com.jobmineplus.mobile.activities.HomeActivity;
import com.jobmineplus.mobile.activities.LoggedInActivityBase;
import com.jobmineplus.mobile.database.jobs.JobDataSource;
import com.jobmineplus.mobile.database.pages.PageDataSource;
import com.jobmineplus.mobile.database.users.UserDataSource;
import com.jobmineplus.mobile.debug.DebugHomeActivity;
import com.jobmineplus.mobile.exceptions.HiddenColumnsException;
import com.jobmineplus.mobile.exceptions.InfiniteLoopException;
import com.jobmineplus.mobile.exceptions.JbmnplsLoggedOutException;
import com.jobmineplus.mobile.exceptions.JbmnplsParsingException;
import com.jobmineplus.mobile.widgets.DatabaseTask;
import com.jobmineplus.mobile.widgets.DatabaseTask.Action;
import com.jobmineplus.mobile.widgets.DatabaseTask.IDatabaseTask;
import com.jobmineplus.mobile.widgets.Job;
import com.jobmineplus.mobile.widgets.ProgressDialogAsyncTaskBase;
import com.jobmineplus.mobile.widgets.StopWatch;

public abstract class JbmnplsActivityBase extends LoggedInActivityBase implements IDatabaseTask<Long>, DialogInterface.OnClickListener {

    // =================
    // Declarations
    // =================
    protected static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    protected final static String EXTRA_JOB_ID = "jobId";

    private String dataUrl = null; // Use JbmnPlsHttpService.GET_LINKS.<url>

    protected ArrayList<Job> allJobs;
    protected GetHtmlTask task = null;
    protected JobDataSource jobDataSource;
    protected PageDataSource pageDataSource;
    protected long timestamp;
    private String pageName;
    private Boolean backBtnDisabled = false;
    private DatabaseTask<Long> databaseTask;
    private Builder confirm;
    private Bundle savedInstance;

    // ====================
    // Abstract Methods
    // ====================

    public abstract String getPageName();

    public abstract String getUrl();

    /**
     * Here you are given the document of the dataUrl page specified in setUp().
     * Also render the layout with the data here.
     *
     * @param doc
     */
    protected abstract void parseWebpage(String html);

    /**
     * Calling this when the parseWebpage is complete. This is used when you
     * need to update any visual element that you cannot do in parseWebpage
     *
     * @param doc
     */
    protected abstract void onRequestComplete(boolean pulledData);

    protected abstract long doOffine();


    // ====================
    // Override Methods
    // ====================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstance = savedInstanceState;
        allJobs = new ArrayList<Job>();
        jobDataSource = new JobDataSource(this);
        pageDataSource = new PageDataSource(this);
        jobDataSource.open();
        pageDataSource.open();
        confirm = new Builder(this);
        confirm.setPositiveButton("Yes", this).setNegativeButton("No", this)
            .setMessage(getString(R.string.go_offline_message));
        dataUrl = getUrl();
        pageName = getPageName();

        getSupportActionBar().setTitle(pageName.substring(pageName.lastIndexOf(".") + 1));
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Coming from home we want to request data, returning after clearing ram, do offline
        if (savedInstance == null) {
            requestData();
        } else {
            doExecuteGetTask();
        }
    }

    @Override
    protected void onDestroy() {
        jobDataSource.close();
        pageDataSource.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!backBtnDisabled) {
            super.onBackPressed();
        }
    }

    // ========================
    // Confirm dialogue click
    // ========================
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
        case DialogInterface.BUTTON_POSITIVE:
            setOnlineMode(false);
            doExecuteGetTask();
            break;
        case DialogInterface.BUTTON_NEGATIVE:
            goToHomeActivity();
            break;
        }
    }

    // ========================
    // Login/Logout Methods
    // ========================
    protected boolean verifyLogin() {
        // TODO put this in a lower case so that Loginactivity can use it as well
        if (getLastUser() == null) {
            return false;
        }
        return client.verifyLogin();
    }

    protected Pair<String, String> getLastUser() {
        String username = client.getUsername();
        String password = client.getPassword();
        if (username == null || password == null) {
            UserDataSource userDataSource = new UserDataSource(this);
            userDataSource.open();
            Pair<String, String> credentials = userDataSource.getLastUser();
            if (credentials != null) {
                client.setLoginCredentials(credentials.first, credentials.second);
            }
            userDataSource.close();
            return credentials;
        } else {
            return new Pair<String, String>(username, password);
        }
    }

    // ======================
    // Activity Movements
    // ======================
    protected void goToHomeActivity(String reasonMsg) {
        if (isDebug()) {
            startActivityWithMessage(DebugHomeActivity.class, reasonMsg);
        } else {
            startActivityWithMessage(HomeActivity.class, reasonMsg);
        }
    }

    protected void goToHomeActivity() {
        if (isDebug()) {
            startActivity(DebugHomeActivity.class);
        } else {
            startActivity(HomeActivity.class);
        }
    }

    protected void goToDescription(int jobId) {
        BasicNameValuePair pass = new BasicNameValuePair(EXTRA_JOB_ID,
                Integer.toString(jobId));
        startActivity(Description.class, pass);
    }

    protected void startActivityWithMessage(Class<?> cls, String reasonMsg) {
        Intent in = new Intent(this, cls);
        in.putExtra(HomeActivity.INTENT_REASON, reasonMsg);
        startActivity(in);
        finish();
    }

    protected void startActivity(Class<?> goToClass) {
        NameValuePair[] empty = null;
        startActivity(goToClass, empty);
    }

    protected void startActivity(Class<?> goToClass, NameValuePair... args) {
        Intent in = new Intent(this, goToClass);
        if (args != null) {
            for (NameValuePair arg : args) {
                in.putExtra(arg.getName(), arg.getValue());
            }
        }
        startActivity(in);
    }

    // ======================
    // Database Task Members
    // ======================
    @Override
    public Long doPutTask() {
        backBtnDisabled = true;

        // Shallow copy of all jobs so that it avoids concurrency issues
        ArrayList<Job> copyAllJobs = new ArrayList<Job>(allJobs);
        jobDataSource.addJobs(copyAllJobs);
        if (pageName != null) {
            pageDataSource.addPage(client.getUsername(), pageName, copyAllJobs, timestamp);
        }
        backBtnDisabled = false;
        return null;
    }

    @Override
    public Long doGetTask() {
        // Handle the moments when username is null, eg. coming back from clearing ram
        if (getLastUser() == null) {
            return (long) -1;
        }
        return doOffine();
    }

    @Override
    public void finishedTask(Long result, DatabaseTask.Action action) {
        if (action == Action.GET) {
            if (result > 0) {
                getSupportActionBar().setSubtitle(formatDateFromNow(result, "Last accessed"));
            } else if (result == 0) {
                getSupportActionBar().setSubtitle(getString(R.string.never_accessed_before));
            } else {
                throw new IllegalStateException("It is impossible to go to this screen without logging in.");
            }
            onRequestComplete(false);
        }
    }

    private void doExecuteGetTask() {
        getSupportActionBar().setSubtitle(" ");
        databaseTask = new DatabaseTask<Long>(this);
        databaseTask.executeGet();
    }

    // =================
    // Miscellaneous
    // =================
    protected String formatDateFromNow(Date then) {
        return formatDateFromNow(then.getTime());
    }
    protected String formatDateFromNow(long then) {
        return formatDateFromNow(then, null);
    }
    protected String formatDateFromNow(long then, String prefix) {
        Date now = new Date();
        prefix = (prefix == null) ? "" : prefix.trim();
        StringBuilder sb = new StringBuilder(prefix).append(" ");
        float diffSecs = Math.round((now.getTime() - then) / 1000);
        if (diffSecs > (60*60*24)) {                    // Return the parsed Date
            return sb.append("on ")
                    .append(DISPLAY_DATE_FORMAT.format(then)).toString();
        } else {
            if (diffSecs < 60) {                        // Seconds
                sb.append((int)diffSecs + " second");
            } else if (diffSecs < (60*60)) {            // Minutes
                diffSecs = Math.round(diffSecs/60);
                sb.append((int)diffSecs + " minute");
            } else {                                    // Hours
                diffSecs = Math.round(diffSecs/(60*60));
                sb.append((int)diffSecs + " hour");
            }
            if (diffSecs > 1) {
                sb.append("s ago");
            } else {
                sb.append(" ago");
            }
            return sb.toString();
        }
    }

    protected void jobsToDatabase() {
        if (isReallyOnline()) {
            databaseTask = new DatabaseTask<Long>(this);
            databaseTask.executePut();
        }
    }

    protected void addJob(Job job) {
        allJobs.add(job);
    }

    protected boolean isLoading() {
        return task != null && task.isRunning();
    }

    // ====================================
    // Data Request Classes and Methods
    // ====================================

    protected void requestData() throws RuntimeException {
        if (isOnline()) {
            if (!isReallyOnline()) {
                confirm.show();
            } else {
                task = new GetHtmlTask(this, getString(R.string.login_message));
                task.execute(dataUrl);
            }
        } else {
            doExecuteGetTask();
        }
    }

    /**
     * You can override this function if you need to fetch something else
     * besides the default url. Return null if it failed and this class will
     * throw a dialog saying it failed otherwise return the html
     *
     * @param url
     * @return null if failed or String that is the html
     * @throws IOException
     */
    protected String onRequestData(String[] args)
            throws JbmnplsLoggedOutException, IOException {
        String url = args[0];
        return client.getJobmineHtml(url);
    }

    private class GetHtmlTask extends
            ProgressDialogAsyncTaskBase<String, String, Integer> {

        static final int NO_PROBLEM = 0;
        static final int FORCED_LOGGEDOUT = 1;
        static final int GO_HOME_NO_REASON = 2;
        static final int HIDDEN_COLUMNS_ERROR = 3;
        static final int PARSING_ERROR = 4;
        static final int NETWORK_ERROR = 5;
        static final int INFINITE_LOOP_ERROR = 6;

        private final StopWatch sw = new StopWatch();
        public GetHtmlTask(Activity activity, String dialogueMessage) {
            super(activity, dialogueMessage, isOnline());
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            setMessage(values[0]);
        }

        @Override
        protected Integer doInBackground(String... params) {
            JbmnplsActivityBase activity = (JbmnplsActivityBase) getActivity();

            if (!verifyLogin()) {
                return FORCED_LOGGEDOUT;
            }
            publishProgress(getString(R.string.wait_post_message));

            sw.start();
            String html = "empty";
            try {
                backBtnDisabled = true;
                html = activity.onRequestData(params);
                timestamp = System.currentTimeMillis();
                if (html == null) {
                    backBtnDisabled = false;
                    return NETWORK_ERROR;
                }
                activity.parseWebpage(html);
                return NO_PROBLEM;
            } catch (InfiniteLoopException e) {
                e.printStackTrace();
                return INFINITE_LOOP_ERROR;
            } catch (HiddenColumnsException e) {
                e.printStackTrace();
                return HIDDEN_COLUMNS_ERROR;
            } catch (JbmnplsParsingException e) {
                e.printStackTrace();
                return PARSING_ERROR;
            } catch (JbmnplsLoggedOutException e) {
                e.printStackTrace();
                return FORCED_LOGGEDOUT;
            } catch (SSLException e) {      // Ignore the SSL Error which comes from user losing network signal on get/post
                e.printStackTrace();
                return NETWORK_ERROR;
            } catch (IOException e) {
                e.printStackTrace();
                return NETWORK_ERROR;
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            client.abort();
            finish();
        }

        @Override
        protected void onPostExecute(Integer reasonForFailure) {
            super.onPostExecute(reasonForFailure);
            String renderMsg = sw.elapsed() + " ms to render";
            log(renderMsg);
            if (reasonForFailure == NO_PROBLEM) {
                onRequestComplete(true);
            } else {
                switch (reasonForFailure) {
                case INFINITE_LOOP_ERROR:
                    goToHomeActivity(getString(R.string.infinite_loop_error_message));
                    break;
                case FORCED_LOGGEDOUT:
                    confirm.show();
                    break;
                case PARSING_ERROR:
                    goToHomeActivity(getString(R.string.parsing_error_message));
                    break;
                case HIDDEN_COLUMNS_ERROR:
                    goToHomeActivity(getString(R.string.hidden_column_message));
                    break;
                case NETWORK_ERROR:
                    goToHomeActivity(getString(R.string.network_error));
                    break;
                case GO_HOME_NO_REASON:
                    goToHomeActivity("");
                    break;
                }
            }
            backBtnDisabled = false;
        }
    }
}