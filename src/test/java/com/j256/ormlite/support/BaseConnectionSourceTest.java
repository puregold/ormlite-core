package com.j256.ormlite.support;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;

public class BaseConnectionSourceTest extends BaseCoreTest {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void testBasicStuff() throws Exception {
		OurConnectionSource cs = new OurConnectionSource();
		assertFalse(cs.isSavedConnection(createMock(DatabaseConnection.class)));
		DatabaseConnection conn = cs.getReadOnlyConnection();
		assertNull(cs.getSpecialConnection());
		cs.saveSpecialConnection(conn);
		assertSame(conn, cs.getSpecialConnection());
		assertTrue(cs.isSavedConnection(conn));
		assertFalse(cs.isSavedConnection(createMock(DatabaseConnection.class)));
		DatabaseConnection conn2 = cs.getReadOnlyConnection();
		assertSame(conn, conn2);
		cs.clearSpecialConnection(conn);
		assertNull(cs.getSpecialConnection());
		assertFalse(cs.isSavedConnection(conn));
		assertNull(cs.getSavedConnection());
	}

	@Test
	public void testNestedSave() throws Exception {
		OurConnectionSource cs = new OurConnectionSource();
		DatabaseConnection conn = cs.getReadOnlyConnection();
		cs.saveSpecialConnection(conn);
		cs.saveSpecialConnection(conn);
		cs.clearSpecialConnection(conn);
		assertEquals(conn, cs.getSpecialConnection());
	}

	@Test(expected = IllegalStateException.class)
	public void testSaveDifferentConnection() throws Exception {
		OurConnectionSource cs = new OurConnectionSource();
		DatabaseConnection conn = cs.getReadOnlyConnection();
		cs.saveSpecialConnection(conn);
		cs.saveSpecialConnection(createMock(DatabaseConnection.class));
	}

	@Test
	public void testClearNone() throws Exception {
		OurConnectionSource cs = new OurConnectionSource();
		cs.clearSpecialConnection(createMock(DatabaseConnection.class));
	}

	@Test
	public void testClearDifferentConnection() throws Exception {
		OurConnectionSource cs = new OurConnectionSource();
		DatabaseConnection conn = cs.getReadOnlyConnection();
		cs.saveSpecialConnection(conn);
		cs.clearSpecialConnection(createMock(DatabaseConnection.class));
	}

	private class OurConnectionSource extends BaseConnectionSource {

		public DatabaseConnection getReadOnlyConnection() throws SQLException {
			return getReadWriteConnection();
		}

		public DatabaseConnection getReadWriteConnection() throws SQLException {
			DatabaseConnection conn = getSavedConnection();
			if (conn == null) {
				return conn;
			} else {
				return createMock(DatabaseConnection.class);
			}
		}

		public void releaseConnection(DatabaseConnection connection) throws SQLException {
			// noop
		}

		public boolean saveSpecialConnection(DatabaseConnection connection) {
			return saveSpecial(connection);
		}

		public void clearSpecialConnection(DatabaseConnection connection) {
			clearSpecial(connection, logger);
		}

		public void close() throws SQLException {
			// noop
		}

		public DatabaseType getDatabaseType() {
			return databaseType;
		}
	}
}
