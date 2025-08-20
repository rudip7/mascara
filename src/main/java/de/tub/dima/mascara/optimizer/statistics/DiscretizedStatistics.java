package de.tub.dima.mascara.optimizer.statistics;

public interface DiscretizedStatistics {
    Float getSumAbsoluteFreq();

    boolean isValueInHistogram(String value);
}
