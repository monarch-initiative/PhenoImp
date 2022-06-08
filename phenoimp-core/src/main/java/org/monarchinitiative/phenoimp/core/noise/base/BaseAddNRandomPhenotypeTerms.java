package org.monarchinitiative.phenoimp.core.noise.base;

import com.google.protobuf.Message;
import org.monarchinitiative.phenoimp.core.noise.PhenopacketNoise;
import org.monarchinitiative.phenoimp.core.noise.util.RandomOntologyTermGenerator;
import org.monarchinitiative.phenol.constants.hpo.HpoSubOntologyRootTermIds;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class BaseAddNRandomPhenotypeTerms<T extends Message> implements PhenopacketNoise<T> {

    private final RandomOntologyTermGenerator termGenerator;
    private final int numberOfTermsToAdd;
    /**
     * Create an instance with randomness seeded by provided seed.
     *
     * @param ontology ontology to use.
     * @param numberOfTermsToAdd number of terms to add to the phenopacket.
     * @param randomSeed random seed
     */
    protected BaseAddNRandomPhenotypeTerms(Ontology ontology, int numberOfTermsToAdd, long randomSeed) {
        Ontology phenotypicAbnormality = Objects.requireNonNull(ontology).subOntology(HpoSubOntologyRootTermIds.PHENOTYPIC_ABNORMALITY);
        this.termGenerator = new RandomOntologyTermGenerator(phenotypicAbnormality, randomSeed);
        this.numberOfTermsToAdd = numberOfTermsToAdd;
    }


    protected List<Term> selectRandomTerms(Set<TermId> presentTermIds) {
        List<Term> randomTerms = new ArrayList<>(numberOfTermsToAdd);

        while (randomTerms.size() < numberOfTermsToAdd && termGenerator.hasNext()) {
            Term term = termGenerator.next();
            if (presentTermIds.contains(term.id()))
                // we should not add already present term
                // TODO - should we care if we're adding a parent/child of an already existing term?
                continue;

            presentTermIds.add(term.id()); // Ensure we do not choose the same term twice
            randomTerms.add(term);
        }
        return randomTerms;
    }

}
