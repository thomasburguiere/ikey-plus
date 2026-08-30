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
    public void init(final ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        try {

            final String cronSchedule = getInitParameter("cronSchedule");
            System.out.println(cronSchedule);

            // Get Servlet Context
            final ServletContext servletContext = getServletContext();
            // Get Schedule Factory from servlet sontext
            final SchedulerFactory schedulerFactory = (SchedulerFactory) servletContext
                    .getAttribute(QuartzInitializerServlet.QUARTZ_FACTORY_KEY);

            final Scheduler scheduler = schedulerFactory.getScheduler();

            final JobDetail job = newJob(Worker.class).withIdentity("deleteJob", "deleteGroup").build();

            final CronTrigger cronTrigger = newTrigger().withIdentity("cronTrigger", "triggerGroup")
                    .withSchedule(cronSchedule(cronSchedule)).startNow().build();

            scheduler.scheduleJob(job, cronTrigger);

        } catch (final SchedulerException | ParseException e) {
            e.printStackTrace();

        }

    }

    @Override
    public void service(final ServletRequest servletRequest, final ServletResponse servletResponse)
            throws ServletException, IOException {
        // nothing to do here
    }

}