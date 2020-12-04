package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DBAccessor {
	private String dbname;
	private String host;
	private String port;
	private String user;
	private String passwd;
	private String schema;
	Connection conn = null;

	/**
	 * Initializes the class loading the database properties file and assigns
	 * values to the instance variables.
	 * 
	 * @throws RuntimeException
	 *             Properties file could not be found.
	 */
	public void init() {
		Properties prop = new Properties();
		InputStream propStream = this.getClass().getClassLoader().getResourceAsStream("db.properties");

		try {
			prop.load(propStream);
			this.host = prop.getProperty("host");
			this.port = prop.getProperty("port");
			this.dbname = prop.getProperty("dbname");
			this.schema = prop.getProperty("schema");
		} catch (IOException e) {
			String message = "ERROR: db.properties file could not be found";
			System.err.println(message);
			throw new RuntimeException(message, e);
		}
	}

	/**
	 * Obtains a {@link Connection} to the database, based on the values of the
	 * <code>db.properties</code> file.
	 * 
	 * @return DB connection or null if a problem occurred when trying to
	 *         connect.
	 */
	public Connection getConnection(Identity identity) {

		// Implement the DB connection
		String url = null;
		try {
			// Loads the driver
			Class.forName("org.postgresql.Driver");

			// Preprara connexió a la base de dades
			StringBuffer sbUrl = new StringBuffer();
			sbUrl.append("jdbc:postgresql:");
			if (host != null && !host.equals("")) {
				sbUrl.append("//").append(host);
				if (port != null && !port.equals("")) {
					sbUrl.append(":").append(port);
				}
			}
			sbUrl.append("/").append(dbname);
			url = sbUrl.toString();

			// Utilitza connexió a la base de dades
			conn = DriverManager.getConnection(url, identity.getUser(), identity.getPassword());
			conn.setAutoCommit(false);
		} catch (ClassNotFoundException e1) {
			System.err.println("ERROR: Al Carregar el driver JDBC");
			System.err.println(e1.getMessage());
		} catch (SQLException e2) {
			System.err.println("ERROR: No connectat  a la BD " + url);
			System.err.println(e2.getMessage());
		}

		// Sets the search_path
		if (conn != null) {
			Statement statement = null;
			try {
				statement = conn.createStatement();
				statement.executeUpdate("SET search_path TO " + this.schema);
				// missatge de prova: verificació
				System.out.println("OK: connectat a l'esquema " + this.schema + " de la base de dades " + url
						+ " usuari: " + user + " password:" + passwd);
				System.out.println();
				//
			} catch (SQLException e) {
				System.err.println("ERROR: Unable to set search_path");
				System.err.println(e.getMessage());
			} finally {
				try {
					statement.close();
				} catch (SQLException e) {
					System.err.println("ERROR: Closing statement");
					System.err.println(e.getMessage());
				}
			}
		}

		return conn;
	}


	public void altaAutor() throws SQLException, IOException {
		Scanner reader = new Scanner(System.in);
		System.out.println("Introdueix el id de l'autor");
		int id = reader.nextInt();
		System.out.println("Introdueix el nom");
		reader.nextLine();
		String nom = reader.nextLine();
		System.out.println("Introdueix l'any de naixement");
		String any_naixement = reader.nextLine();
		System.out.println("Introdueix la nacionalitat");
		String nacionalitat = reader.nextLine();
		System.out.println("Es actiu? (S/N)");
		String actiu = reader.nextLine();
		
		Statement statement = null;
		statement = conn.createStatement();
		statement.executeUpdate("INSERT INTO autors VALUES ("+id+",'"+nom+"','"+any_naixement+"','"+nacionalitat+"','"+actiu+"')");


	}

	public void altaRevista() throws SQLException, NumberFormatException, IOException, ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Scanner reader = new Scanner(System.in);
		System.out.println("Introdueix el id de la revista");
		int id = reader.nextInt();
		System.out.println("Introdueix el titol");
		reader.nextLine();
		String titol = reader.nextLine();
		System.out.println("Introdueix la data de publicacio (yyyy-mm-dd)");
		Date date = format.parse(reader.nextLine());
		
		Statement statement = null;
		statement = conn.createStatement();
		statement.executeUpdate("INSERT INTO revistes (id_revista, titol, data_publicacio) VALUES ("+id+",'"+titol+"','"+date+"')");

	}


	public void altaArticle() throws SQLException, NumberFormatException, IOException, ParseException {

		// TODO demana per consola els valors dels diferents atributs
		// d'article, excepte aquells que poden ser nuls , i realitza la
		// inserció d'un registre


		
	}
	
	public void afegeixArticleARevista(Connection conn) throws SQLException {

		ResultSet rs = null;
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		try {
			rs = st.executeQuery("SELECT * FROM articles WHERE id_revista IS NULL");

			if (rs.getFetchSize() == 0) {
				System.out.println("No hi ha articles pendents d'associar revistes. ");
			} else {
				while (rs.next()) {
					System.out.println("Titol: " + rs.getString("titol"));
					
					System.out.println("Vol incorporar aquest article a una revista?");
					String resposta = br.readLine();

					if (resposta.equals("si")) {
						// demana l'identificador de la revista
						System.out.println("Introdueix el id de la revista");
						int idRevista = Integer.parseInt(br.readLine());
						// actualitza el camp
						rs.updateInt("id_revista", idRevista);
						// actualitza la fila
						rs.updateRow();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	// TODO
	public void actualitzarTitolRevistes(Connection conn) throws SQLException {
		// TODO
		// seguint l'exemple de la funció afegeixArticleARevista:
		// definir variables locals
		// realitzar la consulta de totes les revistes
		// mentre hi hagi revistes:
		// Mostrar el títol de la revista
		// demanar si es vol canviar el seu títol
		// en cas de que la resposta sigui "si"
		// demanar el nou títol per la revista
		// actualitzar el camp
		// actualitzar la fila
		
	
	}
	

	// TODO
	public void desassignaArticleARevista(Connection conn) throws SQLException, IOException {

		// TODO
		// seguint l'exemple de la funció afegeixArticleARevista:
		// definir variables locals
		// sol·licitar l'identificador de la revista
		// realitzar la consulta de tots els articles que corresponen a aquesta
		// revista
		// si no hi ha articles, emetre el missatge corresponent
		// en altre cas, mentre hi hagi articles:
		// Mostrar el títol de l'article i l'identificador de la revista
		// demanar si es vol rescindir la seva incorporació a la revista
		// en cas de que la resposta sigui "si"
		// actualitzar el camp corresponent a null
		// actualitzar la fila
		// en altre cas imprimir "operació cancel·lada"

		
	}

	
	public void mostraAutors() throws SQLException, IOException {
		Statement st = conn.createStatement();
		Scanner reader = new Scanner(System.in);
		ResultSet rs;

		rs = st.executeQuery("SELECT * FROM autors");
		while (rs.next()) System.out.println("ID: " +rs.getString("id_autor") + "\tNom: " + rs.getString("nom") + "\tAny Naixement: " + rs.getString("any_naixement") + "\tNacionalitat: " + rs.getString("nacionalitat") + "\tActiu: " + rs.getString("actiu"));
		rs.close();
		st.close();
	}

	
	public void mostraRevistes() throws SQLException, IOException {
		Statement st = conn.createStatement();
		Scanner reader = new Scanner(System.in);
		ResultSet rs;

		rs = st.executeQuery("SELECT * FROM revistes");
		while (rs.next()) System.out.println("ID: " +rs.getString(1) + "\tTitol: " + rs.getString(2) + "\tData Publicacio: " + rs.getString(3));
		rs.close();
		st.close();
	}

	
	public void mostraRevistesArticlesAutors() throws SQLException, IOException {
		Statement st = conn.createStatement();
		Scanner reader = new Scanner(System.in);
		ResultSet rs;

		rs = st.executeQuery("SELECT a.nom, r.titol, ar.titol FROM autors a, revistes r, articles ar WHERE ar.id_autor=a.id_autor AND ar.id_revista=r.id_revista");
		while (rs.next()) System.out.println("Nom autor: " +rs.getString(1) + "\tNomRevista: " + rs.getString(2) + "\tNom article: " + rs.getString(3));
		rs.close();
		st.close();

	}

	public void sortir() throws SQLException {
		System.out.println("ADÉU!");
		conn.close();
	}
	
	// TODO
	public void carregaAutors(Connection conn) throws SQLException, NumberFormatException, IOException {
		// TODO
		// mitjançant Prepared Statement
		// per a cada línia del fitxer autors.csv
		//realitzar la inserció corresponent

		
	}
}
