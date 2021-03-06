/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mongodb.core.convert;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.data.geo.Box;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;
import org.springframework.data.mongodb.core.convert.GeoConverters.BoxToDbObjectConverter;
import org.springframework.data.mongodb.core.convert.GeoConverters.CircleToDbObjectConverter;
import org.springframework.data.mongodb.core.convert.GeoConverters.DbObjectToBoxConverter;
import org.springframework.data.mongodb.core.convert.GeoConverters.DbObjectToCircleConverter;
import org.springframework.data.mongodb.core.convert.GeoConverters.DbObjectToPointConverter;
import org.springframework.data.mongodb.core.convert.GeoConverters.DbObjectToPolygonConverter;
import org.springframework.data.mongodb.core.convert.GeoConverters.DbObjectToSphereConverter;
import org.springframework.data.mongodb.core.convert.GeoConverters.GeoCommandToDbObjectConverter;
import org.springframework.data.mongodb.core.convert.GeoConverters.PointToDbObjectConverter;
import org.springframework.data.mongodb.core.convert.GeoConverters.PolygonToDbObjectConverter;
import org.springframework.data.mongodb.core.convert.GeoConverters.SphereToDbObjectConverter;
import org.springframework.data.mongodb.core.geo.Sphere;
import org.springframework.data.mongodb.core.query.GeoCommand;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Unit tests for {@link GeoConverters}.
 * 
 * @author Thomas Darimont
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @since 1.5
 */
public class GeoConvertersUnitTests {

	@Test // DATAMONGO-858
	public void convertsBoxToDbObjectAndBackCorrectly() {

		Box box = new Box(new Point(1, 2), new Point(3, 4));

		DBObject dbo = BoxToDbObjectConverter.INSTANCE.convert(box);
		Box result = DbObjectToBoxConverter.INSTANCE.convert(dbo);

		assertThat(result, is(box));
		assertThat(result.getClass().equals(Box.class), is(true));
	}

	@Test // DATAMONGO-858
	public void convertsCircleToDbObjectAndBackCorrectlyNeutralDistance() {

		Circle circle = new Circle(new Point(1, 2), 3);

		DBObject dbo = CircleToDbObjectConverter.INSTANCE.convert(circle);
		Circle result = DbObjectToCircleConverter.INSTANCE.convert(dbo);

		assertThat(result, is(circle));
	}

	@Test // DATAMONGO-858
	public void convertsCircleToDbObjectAndBackCorrectlyMilesDistance() {

		Distance radius = new Distance(3, Metrics.MILES);
		Circle circle = new Circle(new Point(1, 2), radius);

		DBObject dbo = CircleToDbObjectConverter.INSTANCE.convert(circle);
		Circle result = DbObjectToCircleConverter.INSTANCE.convert(dbo);

		assertThat(result, is(circle));
		assertThat(result.getRadius(), is(radius));
	}

	@Test // DATAMONGO-858
	public void convertsPolygonToDbObjectAndBackCorrectly() {

		Polygon polygon = new Polygon(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(5, 6));

		DBObject dbo = PolygonToDbObjectConverter.INSTANCE.convert(polygon);
		Polygon result = DbObjectToPolygonConverter.INSTANCE.convert(dbo);

		assertThat(result, is(polygon));
		assertThat(result.getClass().equals(Polygon.class), is(true));
	}

	@Test // DATAMONGO-858
	public void convertsSphereToDbObjectAndBackCorrectlyWithNeutralDistance() {

		Sphere sphere = new Sphere(new Point(1, 2), 3);

		DBObject dbo = SphereToDbObjectConverter.INSTANCE.convert(sphere);
		Sphere result = DbObjectToSphereConverter.INSTANCE.convert(dbo);

		assertThat(result, is(sphere));
		assertThat(result.getClass().equals(Sphere.class), is(true));
	}

	@Test // DATAMONGO-858
	public void convertsSphereToDbObjectAndBackCorrectlyWithKilometerDistance() {

		Distance radius = new Distance(3, Metrics.KILOMETERS);
		Sphere sphere = new Sphere(new Point(1, 2), radius);

		DBObject dbo = SphereToDbObjectConverter.INSTANCE.convert(sphere);
		Sphere result = DbObjectToSphereConverter.INSTANCE.convert(dbo);

		assertThat(result, is(sphere));
		assertThat(result.getRadius(), is(radius));
		assertThat(result.getClass().equals(org.springframework.data.mongodb.core.geo.Sphere.class), is(true));
	}

	@Test // DATAMONGO-858
	public void convertsPointToListAndBackCorrectly() {

		Point point = new Point(1, 2);

		DBObject dbo = PointToDbObjectConverter.INSTANCE.convert(point);
		Point result = DbObjectToPointConverter.INSTANCE.convert(dbo);

		assertThat(result, is(point));
		assertThat(result.getClass().equals(Point.class), is(true));
	}

	@Test // DATAMONGO-858
	public void convertsGeoCommandToDbObjectCorrectly() {

		Box box = new Box(new double[] { 1, 2 }, new double[] { 3, 4 });
		GeoCommand cmd = new GeoCommand(box);

		DBObject dbo = GeoCommandToDbObjectConverter.INSTANCE.convert(cmd);

		assertThat(dbo, is(notNullValue()));

		DBObject boxObject = (DBObject) dbo.get("$box");

		assertThat(boxObject,
				is((Object) Arrays.asList(GeoConverters.toList(box.getFirst()), GeoConverters.toList(box.getSecond()))));
	}

	@Test // DATAMONGO-1607
	public void convertsPointCorrectlyWhenUsingNonDoubleForCoordinates() {

		assertThat(DbObjectToPointConverter.INSTANCE.convert(new BasicDBObject().append("x", 1L).append("y", 2L)),
				is(new Point(1, 2)));
	}

	@Test // DATAMONGO-1607
	public void convertsCircleCorrectlyWhenUsingNonDoubleForCoordinates() {

		DBObject circle = new BasicDBObject();
		circle.put("center", new BasicDBObject().append("x", 1).append("y", 2));
		circle.put("radius", 3L);

		assertThat(DbObjectToCircleConverter.INSTANCE.convert(circle), is(new Circle(new Point(1, 2), new Distance(3))));
	}

	@Test // DATAMONGO-1607
	public void convertsSphereCorrectlyWhenUsingNonDoubleForCoordinates() {

		DBObject sphere = new BasicDBObject();
		sphere.put("center", new BasicDBObject().append("x", 1).append("y", 2));
		sphere.put("radius", 3L);

		assertThat(DbObjectToSphereConverter.INSTANCE.convert(sphere), is(new Sphere(new Point(1, 2), new Distance(3))));
	}

}
