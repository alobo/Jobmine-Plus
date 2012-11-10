package com.jobmineplus.mobile.widgets;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jobmineplus.mobile.exceptions.JbmnplsException;
import com.jobmineplus.mobile.exceptions.JbmnplsLoggedOutException;
import com.jobmineplus.mobile.exceptions.JbmnplsParsingException;
import com.jobmineplus.mobile.services.JbmnplsHttpService;

public class Job {
    // ================
    //  Constructors
    // ================
    /**
     * As long as all the data is present in the fields (ie. no one hid the
     * columns) then you should be able to fill all of these fields. If user has
     * blocked columns please alert the user to unhide them on the website.
     */

    /**
     * Applications Constructor Since this is the applications, it will consider
     * that you have applied for this job.
     * 
     * @param jId
     * @param jTitle
     * @param jEmployer
     * @param jTerm
     * @param jState
     *            - eg. Job.STATE.FILLED
     * @param jStatus
     *            - eg. Job.STATUS.SCREENED
     * @param jLastDate
     * @param jNumApps
     */
    public Job(int jId, String jTitle, String jEmployer, String jTerm,
            STATE jState, STATUS jStatus, Date jLastDate, int jNumApps) {
        setId(jId);
        title = jTitle;
        employer = jEmployer;
        term = jTerm;
        state = jState;
        status = jStatus;
        lastToApply = jLastDate;
        numApps = jNumApps;
        service = JbmnplsHttpService.getInstance();
    }

    /**
     * Short List Constructor Since this is the job short list, it will consider
     * that you have applied for this job.
     * 
     * @param jId
     * @param jTitle
     * @param jEmployer
     * @param jLocation
     * @param jStatus - eg. Job.STATUS.SCREENED
     * @param jLastDate
     * @param jNumApps
     */
    public Job(int jId, String jTitle, String jEmployer, String jLocation,
            STATUS jStatus, Date jLastDate, int jNumApps) {
        setId(jId);
        title = jTitle;
        employer = jEmployer;
        location = jLocation;
        status = jStatus;
        lastToApply = jLastDate;
        numApps = jNumApps;
        service = JbmnplsHttpService.getInstance();
    }

    /**
     * Job Search Constructor
     * 
     * @param jId
     * @param jTitle
     * @param jEmployer
     * @param jLocation
     * @param jOpenings
     * @param jLastDate
     * @param jNumApps
     */
    public Job(int jId, String jTitle, String jEmployer, String jLocation,
            int jOpenings, Date jLastDate, int jNumApps) {
        setId(jId);
        title = jTitle;
        employer = jEmployer;
        location = jLocation;
        openings = jOpenings;
        lastToApply = jLastDate;
        numApps = jNumApps;
        service = JbmnplsHttpService.getInstance();
    }

    /**
     * Interviews Constructor You must use this for interviews, we assume that
     * you have already applied and you are getting an interview for this job.
     * 
     * @param jId
     * @param jTitle
     * @param jEmployer
     */
    public Job(int jId, String jTitle, String jEmployer) {
        setId(jId);
        title = jTitle;
        employer = jEmployer;
        service = JbmnplsHttpService.getInstance();
    }

   /**
    * Database Constructor This is used for putting all data back into a Job
    * object
    * 
    * @param jId
    * @param jTitle
    * @param jEmployer
    * @param jTerm
    * @param jState
    * @param jStatus
    * @param jLastToApply
    * @param jNumApps
    * @param jOpenings
    * @param jOpenToApply
    * @param jEmployerFull
    * @param jGradesRequired
    * @param jLocation
    * @param jDisciplines
    * @param jLevels
    * @param jHiringSupport
    * @param jWorkSupport
    * @param jDescription
    * @param jWarning
    * @param jInterviewStart
    * @param jInterviewEnd
    * @param jInterviewType
    * @param jRoom
    * @param jInstructions
    * @param jInterviewer
    */
    public Job(int jId, String jTitle, String jEmployer, String jTerm,
            String jState, String jStatus, long jLastToApply, int jNumApps,
            int jOpenings, long jOpenToApply, String jEmployerFull,
            int jGradesRequired, String jLocation, String jDisciplines,
            String jLevels, String jHiringSupport, String jWorkSupport,
            String jDescription, String jWarning, long jInterviewStart, 
            long jInterviewEnd, String jInterviewType, String jRoom, 
            String jInstructions, String jInterviewer) {
        setId(jId);
        title = jTitle;
        employer = jEmployer;
        term = jTerm;
        state = STATE.getStatefromString(jState);
        status = STATUS.getStatusfromString(jStatus);
        lastToApply = new Date(jLastToApply);
        numApps = jNumApps;
        openings = jOpenings;
        openToApply = new Date(jOpenToApply);
        employerFull = jEmployerFull;
        gradesRequired = jGradesRequired == 1;
        location = jLocation;
        disciplines = jDisciplines == null ? null : jDisciplines.split(",");
        levels = jLevels == null ? null : 
            LEVEL.getLevelsFromStrArray(jLevels.split(","));
        hiringSupport = jHiringSupport;
        workSupport = jWorkSupport;
        description = jDescription;
        warning = jWarning;
        interviewStartTime = new Date(jInterviewStart);
        interviewEndTime = new Date(jInterviewEnd);
        interview_type = INTERVIEW_TYPE.getTypefromString(jInterviewType);
        room = jRoom;
        instructions = jInstructions;
        interviewer = jInterviewer;
        service = JbmnplsHttpService.getInstance();
    }
    
    //==========================
    //  Interview Constructors
    //==========================
    /**
     * Main Interviews Constructor
     *  This is the constructor for the first table
     * @param jobId
     * @param employer
     * @param title
     * @param interviewStartTime
     * @param interviewEndTime
     * @param interview_type
     * @param room
     * @param instructions
     * @param interviewer
     * @param jobStatus
     */
    public Job(int jobId, String employer, String title, Date interviewStartTime, Date interviewEndTime, INTERVIEW_TYPE interview_type, 
            String roomInfo, String instructions, String interviewer) throws JbmnplsParsingException{
        setId(jobId);
        if (interview_type == INTERVIEW_TYPE.GROUP || interview_type == INTERVIEW_TYPE.CANCELLED || interview_type == INTERVIEW_TYPE.SPECIAL) {
            throw new JbmnplsParsingException("You used the wrong constructor for this interviews interview_type!");
        }
        this.interview_type = interview_type;
        setEmployer(employer);
        setTitle(title);
        setInterviewStartTime(interviewStartTime);
        setInterviewEndTime(interviewEndTime);
        setRoomInfo(roomInfo);
        setInstructions(instructions);
        setInterviewer(interviewer);
    }
    
    /**
     * Group Interviews Constructor
     *  This is the constructor for the 2nd table
     * @param jobId
     * @param employer
     * @param title
     * @param interviewStartTime
     * @param interviewEndTime
     * @param roomInfo
     * @param instructions
     */
    public Job(int jobId, String employer, String title, Date interviewStartTime, 
            Date interviewEndTime, String roomInfo, String instructions) {
        setId(jobId);
        setEmployer(employer);
        setTitle(title);
        setInterviewStartTime(interviewStartTime);
        setInterviewEndTime(interviewEndTime);
        this.interview_type = INTERVIEW_TYPE.GROUP;
        setRoomInfo(roomInfo);
        setInstructions(instructions);
    }
    
    /**
     * Special Interviews Constructor
     *  This is the constructor for the 3rd table
     * @param jobId
     * @param employer
     * @param title
     * @param instructions
     */
    public Job(int jobId, String employer, String title, String instructions) {
        setId(jobId);
        setEmployer(employer);
        setTitle(title);
        this.interview_type = INTERVIEW_TYPE.SPECIAL;
        setInstructions(instructions);
    }

    // ===========================
    //  Static properties used
    // ===========================

    static public enum STATE {
        CANCELLED("Cancelled", 9), 
        AVAILABLE("Applications Avaliable", 2), 
        FILLED("Filled", 6), 
        POSTED("Posted", 1), 
        SCREENED("Screened", 4), 
        RANKING_COMPLETE("Ranking Complete", 8), 
        SCHEDULED("Scheduled", 5), 
        APPROVED("Approved", 2), 
        COMPLETE("Complete", 2);

        public static STATE getStatefromString(String text)
                throws JbmnplsParsingException {
            if (text == null) {
                return null;
            }
            for (STATE b : STATE.values()) {
                if (text.equalsIgnoreCase(b.toString())) {
                    return b;
                }
            }
            throw new JbmnplsParsingException("State: Cannot match value '"
                    + text + "'");
        }

        public static STATE getDefault() {
            return POSTED;
        }

        @Override
        public String toString() {
            return state;
        }

        public int getPriority() {
            return priority;
        }

        private STATE(String s, int p) {
            state = s;
            priority = p; // The higher the number, the more priority it has
        }

        private String state;
        private int priority;
    }

    static public enum STATUS {
        APPLY("Apply", 3), 
        APPLIED("Applied", 3), 
        ALREADY_APPLIED("Already Applied", 3), 
        CANNOT_APPLY("Not Authorized to Apply", 8), 
        NOT_SELECTED("Not Selected", 10), 
        EMPLOYED("Employed", 11), 
        SELECTED("Selected", 10), 
        ALTERNATE("Alternate", 5), 
        BLANK("", 0);

        public static STATUS getStatusfromString(String text)
                throws JbmnplsParsingException {
            if (text == null) {
                return null;
            }
            for (STATUS b : STATUS.values()) {
                if (text.equalsIgnoreCase(b.toString())) {
                    return b;
                }
            }
            throw new JbmnplsParsingException("Status: Cannot match value '"
                    + text + "'");
        }

        public static STATUS getDefault() {
            return BLANK;
        }

        @Override
        public String toString() {
            return status;
        }

        public int getPriority() {
            return priority;
        }

        private STATUS(String s, int p) {
            status = s;
            priority = p; // The higher the number, the more priority it has
        }

        private String status;
        private int priority;
    }

    static public enum LEVEL {
        JUNIOR("Junior"), INTERMEDIATE("Intermediate"), SENIOR("Senior"), BACHELOR(
                "Bachelor"), MASTERS("Masters"), PHD("Ph.D.");

        public static LEVEL getLevelfromString(String text)
                throws JbmnplsParsingException {
            if (text != null) {
                for (LEVEL b : LEVEL.values()) {
                    if (text.equalsIgnoreCase(b.toString())) {
                        return b;
                    }
                }
            }
            throw new JbmnplsParsingException("State: Cannot match value '"
                    + text + "'");
        }
        
        public static LEVEL[] getLevelsFromStrArray(String[] arr) {
            LEVEL[] levels = new LEVEL[arr.length];
            int count = 0;
            for (String str : arr) {
                levels[count++] = getLevelfromString(str);
            }
            return levels;
        }

        @Override
        public String toString() {
            return state;
        }

        private LEVEL(String s) {
            state = s;
        }

        private String state;
    }
    
    static public enum INTERVIEW_TYPE{
        IN_PERSON   ("In Person"),
        VIDEO       ("Video"),
        PHONE       ("Phone"),
        GROUP       ("Group"),
        SPECIAL     ("Special"),
        CANCELLED   ("Cancelled");
        public static INTERVIEW_TYPE getTypefromString(String text) throws JbmnplsParsingException {
            if (text == null) {
                return null;
            }
            for (INTERVIEW_TYPE interview_type : INTERVIEW_TYPE.values()) {
                String a = interview_type.toString().toLowerCase();
                text = text.toLowerCase(); 
                if (text.contains(a)) {
                    return interview_type;
                }
            }
            throw new JbmnplsParsingException("State: Cannot match value '" + text + "'");
        }
        @Override
        public String toString() {
            return interview_type;
        }
        private INTERVIEW_TYPE(String s) {
            interview_type = s;
        }
        private String interview_type;
    }

    static public final String DESCR_URL_PREFIX = "https://jobmine.ccol.uwaterloo.ca/psc/SS/EMPLOYEE/WORK/c/UW_CO_STUDENTS.UW_CO_JOBDTLS?UW_CO_JOB_ID=";
    private final short NUM_DIGITS_ID = 8;
    private final String REQUIRED_TEXT = "Required";

    protected JbmnplsHttpService service;

    // ===========================
    // Long list of properties
    // ===========================

    // Definitely write once; cannot change
    protected int id;
    protected String title;
    protected String employer;
    protected String term;

    // These can change
    protected STATE state = STATE.getDefault();
    protected STATUS status = STATUS.getDefault();
    protected Date lastToApply;
    protected int numApps;
    protected int openings;

    // Gained from job description
    protected Date openToApply;
    protected String employerFull;
    protected boolean gradesRequired = true;
    protected String location;
    protected String[] disciplines;
    protected LEVEL[] levels;
    protected String hiringSupport;
    protected String workSupport;
    protected String description;
    protected String warning;

    // Interview Data
    private Date interviewStartTime;
    private Date interviewEndTime;
    private INTERVIEW_TYPE interview_type;
    private String room;
    private String instructions;
    private String interviewer;
    
    // Cannot set this
    protected String url = null;

    // Other booleans
    protected boolean hasRead = false;

    // ==============
    // Is Methods
    // ==============
    public boolean areGradesRequired() {
        return gradesRequired;
    }

    public boolean hasRead() {
        return hasRead;
    }

    public boolean hasApplied() {
        return status != STATUS.BLANK && status != STATUS.CANNOT_APPLY;
    }

    public boolean canApply() {
        if (openToApply == null) {
            return false;
        }
        Date now = new Date();
        return now.after(openToApply) && now.before(lastToApply);
    }

    public boolean isOld() {
        return new Date().after(lastToApply);
    }

    public boolean hasDescriptionData() {
        return description != null && description != "";
    }
    
    // This is for interview data
    public boolean hasPassed() {
        if (interviewEndTime == null) {
            return true;
        }
        return new Date().after(interviewEndTime);
    }

    // =============
    // Setters
    // =============
    public void setState(STATE jState) {
        state = jState;
    }

    public void setStatus(STATUS jStatus) {
        status = jStatus;
    }

    public void setLastDateToApply(Date date) {
        lastToApply = date;
    }

    public void setOpeningDateToApply(Date date) {
        openToApply = date;
    }

    public void setNumberOfApplications(int num) {
        numApps = num;
    }

    public void setNumberOfOpenings(int num) {
        openings = num;
    }

    public void setGradesRequired(Boolean flag) {
        gradesRequired = flag;
    }

    public void setLocation(String loc) {
        location = loc;
    }

    public void setLevels(LEVEL[] levelsArr) {
        levels = levelsArr;
    }

    public void setDisciplines(String[] disc) {
        disciplines = disc;
    }

    public void setHiringSupport(String name) {
        hiringSupport = name;
    }

    public void setWorkSupport(String name) {
        workSupport = name;
    }

    public void setDescription(String text) {
        description = text;
    }

    public void setDescriptionWarning(String text) {
        warning = text;
    }

    public void setDescriptionData(
            String fullEmployerName, 
            String title,
            String location, 
            LEVEL[] levelsArr, 
            Date openingDate,
            Date lastDate, 
            boolean areGradesRequired, 
            int numOpenings,
            String[] disciplines, 
            String workSupportName,
            String hiringSupportName, 
            String descriptionWarning,
            String description) {
        setEmployerFullName(fullEmployerName);
        setTitle(title);
        setLocation(location);
        setLevels(levelsArr);
        setOpeningDateToApply(openingDate);
        setLastDateToApply(lastDate);
        setGradesRequired(areGradesRequired);
        setNumberOfOpenings(numOpenings);
        setDisciplines(disciplines);
        setWorkSupport(workSupportName);
        setHiringSupport(hiringSupportName);
        setDescriptionWarning(descriptionWarning);
        setDescription(description);
    }

    // Protected
    protected void setTitle(String jTitle) {
        title = jTitle;
    }

    protected void setEmployer(String jEmployer) {
        employer = jEmployer;
    }

    protected void setTerm(String jTerm) {
        term = jTerm;
    }

    protected void setEmployerFullName(String name) {
        employerFull = name;
    }

    protected void setReadAlready(boolean flag) {
        hasRead = flag;
    }

    protected void setId(int jId) throws IllegalArgumentException {
        if (jId <= 0) {
            throw new IllegalArgumentException(
                    "You cannot set an id that is negative or equal to 0.");
        }
        id = jId;
        String str_id = String.valueOf(jId);
        while (str_id.length() < NUM_DIGITS_ID) {
            str_id = "0" + str_id;
        }
        url = Job.DESCR_URL_PREFIX + str_id;
    }
    
    public void setInterviewStartTime(Date start) {
        interviewStartTime = start;
    }
    
    public void setInterviewEndTime(Date end) {
        interviewEndTime = end;
    }
    
    public void setRoomInfo(String roomInfo) {
        room = roomInfo;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public void setInterviewer(String name) {
        interviewer = name;
    }

    // =============
    // Getters
    // =============
    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getEmployer() {
        return employer != null ? employer : employerFull;
    }

    public String getTerm() {
        return term;
    }

    public STATE getState() {
        return state;
    }

    public STATUS getStatus() {
        return status;
    }

    public Date getLastDateToApply() {
        return lastToApply;
    }

    public Date getOpenDateToApply() {
        return openToApply;
    }

    public int getNumberOfApplications() {
        return numApps;
    }

    public int getNumberOfOpenings() {
        return openings;
    }

    public String getLocation() {
        return location;
    }

    public String[] getDisciplines() {
        return disciplines;
    }

    public String getDisciplinesAsString() {
        return arrayJoin(disciplines, ",");
    }

    public LEVEL[] getLevels() {
        return levels;
    }

    public String getLevelsAsString() {
        return arrayJoin(levels, ",");
    }

    public String getHiringSupportName() {
        return hiringSupport;
    }

    public String getWorkSupportName() {
        return workSupport;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayStatus() {
        return status.getPriority() > state.getPriority() ? status.toString()
                : state.toString();
    }

    public String getDescriptionWarning() {
        return warning;
    }

    public String getEmployerFullName() {
        return employerFull;
    }
    
    public Date getInterviewStartTime() {
        return interviewStartTime;
    }
    
    public Date getInterviewEndTime() {
        return interviewEndTime;
    }
    
    public int getInterviewLengthInMinutes() {
        int length = (int)((interviewEndTime.getTime() - interviewStartTime.getTime()) / 1000) / 60;
        if (length <= 0) {
            throw new JbmnplsException("Either start or end time is not set, and therefore " +
                    "cannot get 0 or negative length in time.");
        }
        return length;
    }
    
    public INTERVIEW_TYPE getInterviewType() {
        return interview_type;
    }
    
    public String getRoomInfo() {
        return room;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public String getInterviewer() {
        return interviewer;
    }

    // ===========
    // Methods
    // ===========
    public String grabDescriptionData() {
        Document doc;
        String html;
        Elements spans = null;
        try {
            html = service.getJobmineHtml(url);
            doc = Jsoup.parse(html);
        } catch (JbmnplsLoggedOutException e) {
            e.printStackTrace();
            return null;
        }

        try {
            spans = doc.getElementById("ACE_width").getElementsByTag("span");
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new JbmnplsParsingException(
                    "Cannot parse description, the div's have changed.");
        }

        int count = spans.size();
        String disciplines = "";
        Element span;
        for (int i = 0; i < count; i++) {
            span = spans.get(i);
            switch (i) {
            case 3:
                setOpeningDateToApply(getDateFromElement(span));
                break;
            case 4:
                setLastDateToApply(getDateFromElement(span));
                break;
            case 10:
                setEmployerFullName(getTextFromElement(span));
                break;
            case 12:
                setTitle(getTextFromElement(span));
                break;
            case 14:
                setGradesRequired((getTextFromElement(span)
                        .equals(REQUIRED_TEXT)));
                break;
            case 16:
                setLocation(getTextFromElement(span));
                break;
            case 18:
                setNumberOfOpenings(getIntFromElement(span));
                break;
            case 20:
                disciplines = getTextFromElement(span);
                break;
            case 21:
                String temp = getTextFromElement(span);
                if (temp.length() != 0) {
                    disciplines += "," + temp;
                }
                setDisciplines(disciplines.split(","));
                break;
            case 23:
                String[] thing = getTextFromElement(span).split(",");
                LEVEL[] l = new LEVEL[thing.length];
                for (int j = 0; j < thing.length; j++) {
                    String tempStr = thing[j].trim();
                    if (tempStr.length() != 0) {
                        l[j] = LEVEL.getLevelfromString(tempStr);
                    }
                }
                setLevels(l);
                break;
            case 27:
                setWorkSupport(getTextFromElement(span));
                break;
            case 26:
                setHiringSupport(getTextFromElement(span));
                break;
            case 29:
                setDescriptionWarning(getTextFromElement(span));
                break;
            case 31:
                setDescription(getTextFromElement(span));
                break;
            }
        }
        hasRead = true;
        return html;
    }

    protected String getTextFromElement(Element e) {
        return e.text().replaceAll("\\s+", " ").trim();
    }

    protected Date getDateFromElement(Element e) {
        String text = getTextFromElement(e);
        try {
            return new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)
                    .parse(text);
        } catch (ParseException error) {
            error.printStackTrace();
            return new Date();
        }
    }

    protected int getIntFromElement(Element e) {
        String text = getTextFromElement(e);
        if (text.length() == 0) {
            return 0;
        }
        return Integer.parseInt(text);
    }

    protected double getDoubleFromTD(Element e) {
        String text = getTextFromElement(e);
        return Double.parseDouble(text);
    }

    protected String arrayJoin(Object[] array, String delimiter) {
        if (array == null) {
            return null;
        }
        String returnStr = "";
        int i = 1;
        int size = array.length;
        if (size != 0) {
            returnStr = array[0].toString();
            for (; i < size; i++) {
                returnStr += delimiter + array[i];
            }
        }
        return returnStr;
    }
}
