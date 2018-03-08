package org.evosuite.ga.metaheuristics;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.NoveltyFunction;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NoveltySearch<T extends Chromosome> extends GeneticAlgorithm<T> {

    private final static Logger logger = LoggerFactory.getLogger(NoveltySearch.class);

    private Collection<T> novelArchive = new LinkedHashSet<>();

    private NoveltyFunction<T> noveltyFunction;

    public NoveltySearch(ChromosomeFactory<T> factory) {
        super(factory);

        noveltyFunction = null; //(NoveltyFunction<T>) new BranchNoveltyFunction();
        // setReplacementFunction(new FitnessReplacementFunction());
    }

    public void setNoveltyFunction(NoveltyFunction<T> function) {
        this.noveltyFunction = function;
    }

    /**
     * Sort the population by novelty
     */
    protected void sortPopulation(List<T> population, Map<T, Double> noveltyMap) {
        // TODO: Use lambdas
        Collections.sort(population, Collections.reverseOrder(new Comparator<T>() {
            @Override
            public int compare(Chromosome c1, Chromosome c2) {
                assert ((noveltyMap.get(c1) != null) && (noveltyMap.get(c2) != null)) : "No corresponding values in noveltyMap.";
                return Double.compare(noveltyMap.get(c1), noveltyMap.get(c2));
            }
        }));
    }

    /**
     * Calculate fitness for all individuals
     */
    protected void calculateNoveltyAndSortPopulation() {
        logger.info("Calculating novelty for " + population.size() + " individuals");

        Map<T, Double> noveltyMap = new LinkedHashMap<>();

        for(T individual : population) {
            double novelty = noveltyFunction.getNovelty(individual, population, novelArchive);
            // In theory, the threshold can turn dynamic depending on how many individuals pass or don't pass the initial threshold.
            if (novelty >= Properties.NOVELTY_THRESHOLD) {
                novelArchive.add(individual);
                //adding in the novel archive
            }
            noveltyMap.put(individual, novelty);
        }
        // We may need to pick 'k' individuals from the novelArchive if we only limit 'k' nearest neighbours from the archive to be considered.
        logger.info("Novelty Archive size : "+novelArchive.size());
        // Sort population
        sortPopulation(population, noveltyMap);
    }

    @Override
    public void initializePopulation() {
        notifySearchStarted();
        currentIteration = 0;

        // Set up initial population
        generateInitialPopulation(Properties.POPULATION);

        // Determine novelty
        calculateNoveltyAndSortPopulation();
        this.notifyIteration();
    }

    @Override
    protected void evolve() {

        List<T> newGeneration = new ArrayList<T>();
        logger.info("Populating next generation");

        while (!isNextPopulationFull(newGeneration)) {
            T parent1 = selectionFunction.select(population);
            T parent2 = selectionFunction.select(population);

            T offspring1 = (T)parent1.clone();
            T offspring2 = (T)parent2.clone();

            try {
                if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
                    crossoverFunction.crossOver(offspring1, offspring2);
                }

                notifyMutation(offspring1);
                offspring1.mutate();
                notifyMutation(offspring2);
                offspring2.mutate();

                if(offspring1.isChanged()) {
                    offspring1.updateAge(currentIteration);
                }
                if(offspring2.isChanged()) {
                    offspring2.updateAge(currentIteration);
                }
            } catch (ConstructionFailedException e) {
                logger.info("CrossOver/Mutation failed.");
                continue;
            }

            if (!isTooLong(offspring1))
                newGeneration.add(offspring1);
            else
                newGeneration.add(parent1);

            if (!isTooLong(offspring2))
                newGeneration.add(offspring2);
            else
                newGeneration.add(parent2);
        }

        population = newGeneration;
        //archive
        updateFitnessFunctionsAndValues();
        //
        currentIteration++;
    }

    @Override
    public void generateSolution() {

        if (population.isEmpty())
            initializePopulation();

        logger.info("Starting evolution of novelty search algorithm");

        while (!isFinished()) {
            logger.info("Current population: " + getAge() + "/" + Properties.SEARCH_BUDGET);
            //logger.info("Best fitness: " + getBestIndividual().getFitness());

            evolve();

            // TODO: Sort by novelty
            calculateNoveltyAndSortPopulation();

            this.notifyIteration();
        }
        logger.info("Novelty search finished");

        // updateBestIndividualFromArchive();
        notifySearchFinished();

    }
}
