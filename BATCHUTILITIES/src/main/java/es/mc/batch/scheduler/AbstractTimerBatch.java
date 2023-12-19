package es.mc.batch.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.batch.runtime.BatchRuntime;
import javax.ejb.Schedule;

public abstract class AbstractTimerBatch {

    public static List<Long> executedBatchs = new ArrayList<>();

    @Schedule(hour = "00", minute = "25", second = "0", dayOfWeek = "*", persistent = false)
    public void EliminarLogsFirmaJob() {
        executedBatchs.add(BatchRuntime.getJobOperator().start("eliminarLogsFirmaJob", new Properties()));
        afterRun();
    }

    @Schedule(hour = "06", minute = "00", second = "0", dayOfWeek = "*", persistent = false)
    public void ComunicarExpiracionPermisoJob() {
        executedBatchs.add(BatchRuntime.getJobOperator().start("comunicarExpiracionPermisoJob", new Properties()));
        afterRun();
    }

    protected void afterRun() {

    }
}
