package dao;

import models.Gestionnaire;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestionnaireDAO {
    
    public List<Gestionnaire> getAll() {
        List<Gestionnaire> gestionnaires = new ArrayList<>();
        String sql = "SELECT * FROM gestionnaire";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Gestionnaire g = new Gestionnaire();
                g.setId(rs.getInt("id_gestionnaire"));
                g.setNom(rs.getString("nom"));
                g.setPrenom(rs.getString("prenom"));
                g.setLogin(rs.getString("login"));
                g.setPwd(rs.getString("pwd"));
                gestionnaires.add(g);
            }
            
            System.out.println("DAO: " + gestionnaires.size() + " gestionnaires récupérés");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur GestionnaireDAO.getAll: " + e.getMessage());
            e.printStackTrace();
        }
        
        return gestionnaires;
    }

	public boolean update(Gestionnaire gestionnaire) {
		// TODO Auto-generated method stub
		return false;
	}


}