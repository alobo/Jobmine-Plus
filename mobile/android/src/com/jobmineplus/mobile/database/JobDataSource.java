package com.jobmineplus.mobile.database;

import java.util.Date;

import com.jobmineplus.mobile.widgets.Job;
import com.jobmineplus.mobile.widgets.Job.INTERVIEW_TYPE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class JobDataSource extends DataSourceBase {

    // Database fields
    private JobDatabaseHelper dbHelper;
    
    private final String[] allColumns = {
            JobTable.COLUMN_ID,
            JobTable.COLUMN_TITLE,
            JobTable.COLUMN_EMPLOYER,
            JobTable.COLUMN_TERM,
            JobTable.COLUMN_STATE,
            JobTable.COLUMN_STATUS,
            JobTable.COLUMN_LAST_DATE_APPLY,
            JobTable.COLUMN_NUM_APPS,
            JobTable.COLUMN_OPENINGS,
            JobTable.COLUMN_OPEN_DATE_APPLY,
            JobTable.COLUMN_EMPLOYER_FULL,
            JobTable.COLUMN_GRADE_REQUIRED,
            JobTable.COLUMN_LOCATION,
            JobTable.COLUMN_DISCIPLINES,
            JobTable.COLUMN_LEVELS,
            JobTable.COLUMN_HIRING_SUPPORT,
            JobTable.COLUMN_WORK_SUPPORT,
            JobTable.COLUMN_DESCRIPTION,
            JobTable.COLUMN_DESCRIPTION_WARNING,
            JobTable.COLUMN_INTERVIEW_START_TIME,
            JobTable.COLUMN_INTERVIEW_END_TIME,
            JobTable.COLUMN_INTERVIEW_TYPE,
            JobTable.COLUMN_INTERVIEW_ROOM,
            JobTable.COLUMN_INTERVIEW_INSTRUCTIONS,
            JobTable.COLUMN_INTERVIEWER
    };
    
    public JobDataSource(Context context) {
        dbHelper = new JobDatabaseHelper(context);
    }

    @Override
    public void open() {
        database = dbHelper.getWritableDatabase();
    }
    
    @Override
    public void close() {
        dbHelper.close();
    }

    public synchronized void addJob(Job job) {
        int jobId = job.getId();
        
        // TODO fix this up and match the job merge function
        Date lastDateToApply = job.getLastDateToApply();
        long lastDateTimestamp = lastDateToApply == null ? 0 : lastDateToApply.getTime();
        Date openDateToApply = job.getOpenDateToApply();
        long openDateTimestamp = openDateToApply == null ? 0 : openDateToApply.getTime();
        Date interviewStart = job.getInterviewStartTime();
        long interviewStartTimestamp = interviewStart == null ? 0 : interviewStart.getTime();
        Date interviewEnd = job.getInterviewEndTime();
        long interviewEndTimestamp = interviewEnd == null ? 0 : interviewEnd.getTime();
        
        Job.INTERVIEW_TYPE type = job.getInterviewType();
        
        // Add Date to the columns
        ContentValues values = new ContentValues();
        values.put(JobTable.COLUMN_ID, jobId);
        values.put(JobTable.COLUMN_TITLE, job.getTitle());
        values.put(JobTable.COLUMN_EMPLOYER, job.getEmployer());
        addNonNullValue(values, JobTable.COLUMN_TERM, job.getTerm());
        addNonNullValue(values, JobTable.COLUMN_STATE, job.getState().toString());
        addNonNullValue(values, JobTable.COLUMN_STATUS, job.getStatus().toString());
        addNonNullValue(values, JobTable.COLUMN_LAST_DATE_APPLY, lastDateTimestamp);
        addNonNullValue(values, JobTable.COLUMN_NUM_APPS, job.getNumberOfApplications());
        addNonNullValue(values, JobTable.COLUMN_OPENINGS, job.getNumberOfOpenings());
        addNonNullValue(values, JobTable.COLUMN_OPEN_DATE_APPLY, openDateTimestamp);
        addNonNullValue(values, JobTable.COLUMN_EMPLOYER_FULL, job.getEmployerFullName());
        addNonNullValue(values, JobTable.COLUMN_GRADE_REQUIRED, (job.areGradesRequired() ? 1 : 0));
        addNonNullValue(values, JobTable.COLUMN_LOCATION, job.getLocation());
        addNonNullValue(values, JobTable.COLUMN_DISCIPLINES, job.getDisciplinesAsString());
        addNonNullValue(values, JobTable.COLUMN_LEVELS, job.getLevelsAsString());
        addNonNullValue(values, JobTable.COLUMN_HIRING_SUPPORT, job.getHiringSupportName());
        addNonNullValue(values, JobTable.COLUMN_WORK_SUPPORT, job.getWorkSupportName());
        addNonNullValue(values, JobTable.COLUMN_DESCRIPTION, job.getDescription());
        addNonNullValue(values, JobTable.COLUMN_DESCRIPTION_WARNING, job.getDescriptionWarning());

        // Interview Data
        addNonNullValue(values, JobTable.COLUMN_INTERVIEW_START_TIME, interviewStartTimestamp);
        addNonNullValue(values, JobTable.COLUMN_DESCRIPTION_WARNING, interviewEndTimestamp);
        addNonNullValue(values, JobTable.COLUMN_INTERVIEW_TYPE, type == null ? null : type.toString());
        addNonNullValue(values, JobTable.COLUMN_INTERVIEW_ROOM, job.getRoomInfo());
        addNonNullValue(values, JobTable.COLUMN_INTERVIEW_INSTRUCTIONS, job.getInstructions());
        addNonNullValue(values, JobTable.COLUMN_INTERVIEWER, job.getInterviewer());
        
        updateElseInsert(JobTable.TABLE_JOB, jobId, values);
    }
    
    public synchronized Job getJob(int id) {
        Cursor cursor = database.query(JobTable.TABLE_JOB,
                allColumns, JobTable.COLUMN_ID + " = " + id, null,
                null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        Job job = new Job(
                id,                     // Id
                cursor.getString(1),    // Title
                cursor.getString(2),    // Employer
                cursor.getString(3),    // Term
                cursor.getString(4),    // State
                cursor.getString(5),    // Status
                cursor.getLong(6),      // Last date apply
                cursor.getInt(7),       // Number of apps
                cursor.getInt(8),       // Openings
                cursor.getLong(9),      // Open date to apply
                cursor.getString(10),   // Employer fullname       
                cursor.getInt(11),      // Grades required
                cursor.getString(12),   // Location
                cursor.getString(13),   // Disciplines
                cursor.getString(14),   // Levels
                cursor.getString(15),   // Hiring support
                cursor.getString(16),   // Work support
                cursor.getString(17),   // Description
                cursor.getString(18),   // Description warning
                
                // Interview data
                cursor.getLong(19),     // Interview start time
                cursor.getLong(20),     // Interview end time
                cursor.getString(21),   // Interview type
                cursor.getString(22),   // Interview room
                cursor.getString(23),   // Interview instructions
                cursor.getString(24)    // Interviewer
                );
        cursor.close();
        return job;
    }
    
    public synchronized Job[] getJobsFromIds(int[] ids) {
        //TODO
        return null;
    }

    public synchronized void deleteJob(Job job) {
        // TODO
    }

    public synchronized Job[] getAllJobs() {
        // TODO
        return null;
    }

}
