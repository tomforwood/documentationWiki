package org.forwoods.docuwiki.documentationWiki.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

public class FQClassNameTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testMerge() {
		List<FQClassName> reflected = Stream.of(
				new FQClassName("test", "Test", FQClassName.REFLECTED),
				new FQClassName("test", "Test2", FQClassName.REFLECTED)
			).collect(Collectors.toList());
		List<FQClassName> annotated = Stream.of(
				new FQClassName("test", "Test", FQClassName.ANNOTATED),
				new FQClassName("test", "Test3", FQClassName.ANNOTATED)
			).collect(Collectors.toList());
		
		Map<FQClassName, FQClassName> map = reflected.stream().collect(Collectors.toMap( Function.identity(),  Function.identity()));
		
		annotated.stream().forEach(fqcn -> map.merge(fqcn, fqcn, FQClassName.mergeFunction));
		
		Collection<FQClassName> allValues = map.values();
		Collection<FQClassName> allKeys = map.keySet();

		assertThat(allValues).extracting(fqcn->fqcn.subset).contains(1,2,3);
		assertThat(allKeys).extracting(fqcn->fqcn.subset).contains(1,2,3);
		
		
	}

}
