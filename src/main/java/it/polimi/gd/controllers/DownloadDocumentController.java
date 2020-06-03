package it.polimi.gd.controllers;

import it.polimi.gd.beans.Document;
import it.polimi.gd.dao.DocumentDao;
import it.polimi.utils.file.FileManager;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/download")
public class DownloadDocumentController extends HttpServlet
{
    private FileManager fileManager;
    private DocumentDao documentDao;

    public DownloadDocumentController()
    {
        super();
    }

    @Override
    public void init()
    {
        fileManager = FileManager.getInstance(getServletContext());
        documentDao = new DocumentDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            int documentId = Integer.parseInt(req.getParameter("doc"));
            Document document = documentDao.findDocumentById(documentId).orElseThrow(FileNotFoundException::new);
            FileInputStream inputStream = fileManager.getFileInputStream(documentId);
            ServletOutputStream outputStream = resp.getOutputStream();

            resp.setHeader("Content-disposition", "attachment; filename="+document.getName()+"."+document.getType().toLowerCase());

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0)
                outputStream.write(buffer, 0, length);
            inputStream.close();
            outputStream.flush();
        }
        catch (NumberFormatException e)
        {
            resp.sendError(400, e.getLocalizedMessage());
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getSQLState());
        }
        catch (FileNotFoundException e)
        {
            resp.sendError(404, "Document not found!");
        }
    }
}
