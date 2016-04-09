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
	@Test
	public void testCompareTo() {
		FQClassName f1 = new FQClassName("test", "test1", FQClassName.REFLECTED);
		FQClassName f2 = new FQClassName("test", "test1", FQClassName.ANNOTATED);
		
		assertThat(f1).isEqualByComparingTo(f2);
		
		FQClassName f3 = new FQClassName(null, "test1", FQClassName.ANNOTATED);
		FQClassName f4 = new FQClassName(null, "test2", FQClassName.ANNOTATED);
		
		assertThat(f3).isLessThan(f4);
		
		assertThat(f3).hasToString("FQClassName [namespace=null, className=test1, subset=2]");
		
	}

}
