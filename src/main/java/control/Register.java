package control;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.DriverManagerConnectionPool;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;
import java.security.SecureRandom;

/**
 * Servlet implementation class Register
 */
@WebServlet("/Register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int ITERATIONS = 10000;
	private static final int KEY_LENGTH = 256;

	private String hashPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
	    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), ITERATIONS, KEY_LENGTH);
	    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	    byte[] hashedPassword = keyFactory.generateSecret(spec).getEncoded();
	    return Base64.getEncoder().encodeToString(hashedPassword);
	}

	private String generateSalt() {
	    SecureRandom random = new SecureRandom();
	    byte[] salt = new byte[16];
	    random.nextBytes(salt);
	    return Base64.getEncoder().encodeToString(salt);
	}
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String salt = generateSalt();
		String hashedPassword = hashPassword(password, salt);
		String nome = request.getParameter("nome");
		String cognome = request.getParameter("cognome");
		String indirizzo = request.getParameter("indirizzo");
		String telefono = request.getParameter("telefono");
		String carta = request.getParameter("carta");
		String intestatario = request.getParameter("intestatario");
		String cvv = request.getParameter("cvv");
		String redirectedPage = "/loginPage.jsp";
		try {
			Connection con = DriverManagerConnectionPool.getConnection();
			String sql = "INSERT INTO UserAccount(email, passwordUser, nome, cognome, indirizzo, telefono, numero, intestatario, CVV) VALUES (?, MD5(?), ?, ?, ?, ?, ?, ?, ?)";
			String sql2 = "INSERT INTO Cliente(email) VALUES (?)";
			String sql3 = "INSERT INTO Venditore(email) VALUES (?)";
			
			//Aggiungi a AccountUser
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, email);
			ps.setString(2, hashedPassword);
			ps.setString(3, salt);
			ps.setString(3, nome);
			ps.setString(4, cognome);
			ps.setString(5, indirizzo);
			ps.setString(6, telefono);
			ps.setString(7, carta);
			ps.setString(8, intestatario);
			ps.setString(9, cvv);
			
			ps.executeUpdate();
			con.commit();
			
			//Aggiungi a Cliente
			PreparedStatement ps2 = con.prepareStatement(sql2);
			ps2.setString(1, email);
			
			ps2.executeUpdate();
			con.commit();
			
			//Aggiungi a Venditore
			PreparedStatement ps3 = con.prepareStatement(sql3);
			ps3.setString(1, email);
			
			ps3.executeUpdate();
			con.commit();
			DriverManagerConnectionPool.releaseConnection(con);
		}
		catch (SQLException e) {
			request.getSession().setAttribute("register-error", true);
			redirectedPage = "/register-form.jsp";
		}
		response.sendRedirect(request.getContextPath() + redirectedPage);
	}
}
