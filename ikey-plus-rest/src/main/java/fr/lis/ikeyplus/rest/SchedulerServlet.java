package fr.lis.ikeyplus.rest;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.ee.servlet.QuartzInitializerServlet;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.text.ParseException;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * this class schedules the deletion of old key files
 *
 * @author Thomas burguiere
 */
public class SchedulerServlet extends GenericServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        try {

            String cronSchedule = getInitParameter("cronSchedule");
            System.out.println(cronSchedule);

            // Get Servlet Context
            ServletContext servletContext = getServletContext();
            // Get Schedule Factory from servlet context
            SchedulerFactory schedulerFactory = (SchedulerFactory) servletContext
                    .getAttribute(QuartzInitializerServlet.QUARTZ_FACTORY_KEY);

            Scheduler scheduler = schedulerFactory.getScheduler();

            JobDetail job = newJob(Worker.class).withIdentity("deleteJob", "deleteGroup").build();

            CronTrigger cronTrigger = newTrigger().withIdentity("cronTrigger", "triggerGroup")
                    .withSchedule(cronSchedule(cronSchedule)).startNow().build();

            scheduler.scheduleJob(job, cronTrigger);

        } catch (SchedulerException | ParseException e) {
            e.printStackTrace();

        }

    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse)
            throws ServletException, IOException {
        // nothing to do here
    }

}