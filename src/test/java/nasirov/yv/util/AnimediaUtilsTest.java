package nasirov.yv.util;

import static nasirov.yv.data.enums.Constants.NOT_FOUND_ON_MAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Set;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class AnimediaUtilsTest extends AbstractTest {

	private AnimediaMALTitleReferences concretizedAndOngoing;

	private AnimediaMALTitleReferences concretizedAndNotOngoing;

	private AnimediaMALTitleReferences updatedTitle;

	private AnimediaMALTitleReferences notUpdatedTitle;

	private AnimediaMALTitleReferences notFoundOnMAL;

	@Before
	public void setUp() {
		Set<AnimediaMALTitleReferences> references = RoutinesIO
				.unmarshalFromResource(referencesForTestResource, AnimediaMALTitleReferences.class, LinkedHashSet.class);
		concretizedAndOngoing = references.stream().filter(ref -> ref.getTitleOnMAL().equals("shingeki no kyojin season 3 part 2")).findFirst()
				.orElse(null);
		concretizedAndNotOngoing = references.stream().filter(ref -> ref.getTitleOnMAL().equals("one punch man: road to hero")).findFirst().orElse(null);
		updatedTitle = references.stream().filter(ref -> ref.getTitleOnMAL().equals("fairy tail")).findFirst().orElse(null);
		notUpdatedTitle = references.stream().filter(ref -> ref.getTitleOnMAL().equals("fairy tail: final series")).findFirst().orElse(null);
		notFoundOnMAL = references.stream().filter(ref -> ref.getTitleOnMAL().equals(NOT_FOUND_ON_MAL.getDescription())).findFirst().orElse(null);
	}

	@Test
	public void isMaxEpisodesUndefined() throws Exception {
		String[] undefinedPositiveVariants = {"x", "xx", "xxx", "X", "XX", "XXX", "х", "хх", "ххх", "Х", "ХХ", "ХХХ"};
		String[] undefinedNegativeVariants = {"", "xxxx", " ", "XXXX", "хххх", "ХХХХ"};
		for (String var : undefinedPositiveVariants) {
			assertTrue(AnimediaUtils.isMaxEpisodesUndefined(var));
		}
		for (String var : undefinedNegativeVariants) {
			assertFalse(AnimediaUtils.isMaxEpisodesUndefined(var));
		}
	}
	@Test
	public void isAnnouncement() throws Exception {
		assertTrue(AnimediaUtils.isAnnouncement(RoutinesIO.readFromResource(htmlWithAnnouncement)));
		assertFalse(AnimediaUtils.isAnnouncement(RoutinesIO.readFromResource(saoHtml)));
	}
	@Test
	public void isTitleConcretizedAndOngoing() throws Exception {
		assertTrue(AnimediaUtils.isTitleConcretizedAndOngoing(concretizedAndOngoing));
		assertFalse(AnimediaUtils.isTitleConcretizedAndOngoing(concretizedAndNotOngoing));
	}
	@Test
	public void isTitleConcretizedOnMAL() throws Exception {
		assertTrue(AnimediaUtils.isTitleConcretizedOnMAL(concretizedAndOngoing));
		assertTrue(AnimediaUtils.isTitleConcretizedOnMAL(concretizedAndNotOngoing));
	}
	@Test
	public void isTitleUpdated() throws Exception {
		assertTrue(AnimediaUtils.isTitleUpdated(updatedTitle));
		assertFalse(AnimediaUtils.isTitleUpdated(notUpdatedTitle));
	}
	@Test
	public void isTitleNotFoundOnMAL() throws Exception {
		assertTrue(AnimediaUtils.isTitleNotFoundOnMAL(notFoundOnMAL));
		assertFalse(AnimediaUtils.isTitleNotFoundOnMAL(concretizedAndNotOngoing));
	}
	@Test
	public void testConstructor() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<?>[] declaredConstructors = AnimediaUtils.class.getDeclaredConstructors();
		assertEquals(1, declaredConstructors.length);
		assertFalse(declaredConstructors[0].isAccessible());
		declaredConstructors[0].setAccessible(true);
		AnimediaUtils fromPrivateConstructor = (AnimediaUtils) declaredConstructors[0].newInstance();
		assertNotNull(fromPrivateConstructor);
	}

}