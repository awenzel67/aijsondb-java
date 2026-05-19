projectname: aijsondbweb

aijsondbweb is a Java Spring boot web application.

The application is used to ask questions in natural language about an excel file. 

The excel file can be uploaded be the user. Aufer sucessufully uploading the file, the file is loaded into the analyser.

After sucessfully uploading the file the user may enter a question about the excel file.

The question is sended to an abstarct analyser. The analyser will respond with a string as answer.

The analyser has a default constructor.

The analyser has the following methods:
```
 public void importData(String fileNameImport)
```
where  fileNameImport is the path to the excel file to import.

```
 public String analyse(String question,Options options)
```
where question is the question entered by the user.

The class Option contains the Model provider: OPENAI, MISTRAL or OLLAMA, the API Key and the Model name.

Do not fully implement the Analyser. Only create dummy methods wich will implement later.

The Options mentioned bevore can also eneterd by the user.



  