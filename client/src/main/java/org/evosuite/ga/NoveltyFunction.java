package org.evosuite.ga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public abstract class NoveltyFunction<T extends Chromosome> {

    private final static Logger logger = LoggerFactory.getLogger(NoveltyFunction.class);

    public abstract double getDistance(T individual1, T individual2);

    public double getNovelty(T individual, Collection<T> population, Collection<T> novelArchive) {
        double distance = 0.0;
        for(T other : population) {
            if(other == individual)
                continue;

            double d = getDistance(individual, other);
            distance += d;
        }
        for(T other : novelArchive) {
            if(other == individual)
                continue;

            double d = getDistance(individual, other);
            distance += d;
        }

        distance /= (population.size() - 1) + (novelArchive.isEmpty() == true ? 0 : novelArchive.size()-1);
        return distance;
    }
}
