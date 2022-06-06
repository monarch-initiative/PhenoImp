package org.monarchinitiative.hellion.core;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

class RandomOntologyTermGenerator implements Iterator<Term> {

    private final Random random;
    private final TermId[] nonObsoleteTermIds;
    private final Map<TermId, Term> termIdToTerm;

    RandomOntologyTermGenerator(Ontology ontology) {
        this(ontology, Instant.now().getEpochSecond());
    }

    RandomOntologyTermGenerator(Ontology ontology, long randomSeed) {
        this.termIdToTerm = ontology.getTermMap();
        this.nonObsoleteTermIds = ontology.getNonObsoleteTermIds().toArray(TermId[]::new);
        this.random = new Random(randomSeed);
    }

    public Stream<Term> terms() {
        return random.ints(nonObsoleteTermIds.length)
                .mapToObj(idx -> nonObsoleteTermIds[idx])
                .map(termIdToTerm::get);
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Term next() {
        int idx = random.nextInt(nonObsoleteTermIds.length);
        TermId termId = nonObsoleteTermIds[idx];
        return termIdToTerm.get(termId);
    }
}
