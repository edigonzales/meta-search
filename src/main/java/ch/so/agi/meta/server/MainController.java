package ch.so.agi.meta.server;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gargoylesoftware.htmlunit.javascript.host.Console;

import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.gui.UserSettings;
import ch.interlis.ilirepository.Dataset;
import ch.interlis.models.DatasetIdx16.DataFile;
import ch.so.agi.meta.shared.model.DataSet;
import ch.so.agi.meta.shared.model.DataSetFile;
import elemental2.core.JsDate;

@RestController
public class MainController {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return new ResponseEntity<String>("meta search", HttpStatus.OK);
    }
    
    @GetMapping("/ilidata")
    public ResponseEntity<List<DataSet>> ilidata() throws ParseException {
        UserSettings settings = new UserSettings();
        
        // Repositories. Falls nichts gesetzt wird, werden die Standardrepos verwendet (TODO prüfen).
        // Ist das überhaupt notwendig, wenn ich listData machen will?
        //settings.setIlidirs("https://geo.so.ch/models;http://models.interlis.ch");
        
        Configuration config = new Configuration();
        ArrayList ilifilev = new ArrayList();
        //ilifilev.add("http://models.geo.gl.ch");
        ilifilev.add("https://s3.eu-central-1.amazonaws.com/ch.so.geo.repository");
        Iterator ilifilei = ilifilev.iterator();
        while (ilifilei.hasNext()) {
            logger.info("fubar");
            String ilifile = (String) ilifilei.next();
            FileEntry file = new FileEntry(ilifile, FileEntryKind.ILIMODELFILE);
            config.addFileEntry(file);
        }

        List<DataSet> dataSets = new ArrayList<DataSet>();
        List<Dataset> datasets = new ListData().listData(config, settings);
        for (Dataset dataset : datasets) {
//            logger.info(dataset.getMetadata().getid());
//            logger.info(dataset.getMetadata().getfiles()[0].toString());
            
            String id = dataset.getMetadata().getid();
            String title = dataset.getMetadata().gettitle().getLocalisedText()[0].getText();
            String shortDescription = dataset.getMetadata().getshortDescription().getLocalisedText()[0].getText();
            String keywords = dataset.getMetadata().getkeywords();
            String original = dataset.getMetadata().getoriginal();
            String model = dataset.getMetadata().getmodel().getname();
            String modelRepository = dataset.getMetadata().getmodel().getlocationHint();
            String furtherInformation = dataset.getMetadata().getfurtherInformation();
            String furtherMetadata = dataset.getMetadata().getfurtherMetadata();
            String knownWMS = dataset.getMetadata().getknownWMS()[0].getvalue();
            Date lastEditingDate = null;
            try {
                lastEditingDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataset.getMetadata().getlastEditingDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }  
            double westLimit = dataset.getMetadata().getboundary().getwestlimit();
            double southLimit = dataset.getMetadata().getboundary().getsouthlimit();
            double eastLimit = dataset.getMetadata().getboundary().geteastlimit();
            double northLimit = dataset.getMetadata().getboundary().getnorthlimit();

            List<DataSetFile> dataSetFiles = new ArrayList<DataSetFile>();
            DataFile[] dataFiles = dataset.getMetadata().getfiles();
            for (DataFile dataFile : dataFiles) {
                ch.interlis.models.DatasetIdx16.File file = dataFile.getfile()[0];
                String filePath = file.getpath();
                String fileFormat = dataFile.getfileFormat();
                
                DataSetFile dataSetFile = new DataSetFile();
                dataSetFile.location = filePath;
                dataSetFile.format = fileFormat;
                
                dataSetFiles.add(dataSetFile);
            }
            
            DataSet dataSet = new DataSet();
            dataSet.id = id;
            dataSet.title = title;
            dataSet.shortDescription = shortDescription;
            dataSet.keywords = keywords;
            dataSet.original = original;
            dataSet.model = model;
            dataSet.modelRepository = modelRepository;
            dataSet.furtherInformation = furtherInformation;
            dataSet.furtherMetadata = furtherMetadata;
            dataSet.knownWMS = knownWMS;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            dataSet.lastEditingDate = format.format(lastEditingDate);
            dataSet.westLimit = westLimit;
            dataSet.southLimit = southLimit;
            dataSet.eastLimit = eastLimit;
            dataSet.northLimit = northLimit;
            dataSet.files = dataSetFiles.toArray(new DataSetFile[0]);
            
            dataSets.add(dataSet);
        }
        return new ResponseEntity<List<DataSet>>(dataSets, HttpStatus.OK);
    }

}
