module JavaFinance {
	requires javafx.graphics;
	requires javafx.controls;
	requires java.desktop;
	requires javafx.swing;
	requires rxjava;
	requires kryonet;
	requires kryo;
	requires javafx.base;
	requires YahooFinanceAPI;
	requires java.base;
	requires java.sql;
	requires sqlite.jdbc;
	requires java.mail;

	opens src;
}