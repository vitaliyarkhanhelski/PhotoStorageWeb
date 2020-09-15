'use strict';

var singleUploadForm = document.querySelector('#singleUploadForm');
var singleFileUploadInput = document.querySelector('#singleFileUploadInput');
var singleFileUploadError = document.querySelector('#singleFileUploadError');
var singleFileUploadSuccess = document.querySelector('#singleFileUploadSuccess');

function uploadSingleFile(file) {
    var formData = new FormData();
    formData.append("file", file);

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/api/v1/photos");

    xhr.onload = function () {
        var response = JSON.parse(xhr.responseText);
        if (xhr.status == 201) {
            singleFileUploadError.style.display = "none";
            singleFileUploadSuccess.innerHTML = "<p>File Uploaded Successfully.</p>"+
            "<p><b>"+response.fileName+"</b></p><p> DownloadUrl : <a href='" + response.fileDownloadUri + "' target='_blank'>" + response.fileDownloadUri + "</a></p>";
            singleFileUploadSuccess.style.display = "block";
        } else {
            singleFileUploadSuccess.style.display = "none";
            singleFileUploadError.innerHTML = (response && response.message) || "Some Error Occurred";
        }
        if(xhr.status == 403) {
            singleFileUploadError.innerHTML = "<p style=color:red;><b>Error! 403</b></p><p>Forbidden Type: "+response.fileType+"</p>";
            singleFileUploadError.style.display = "block";
        }
        if (xhr.status == 302) {
            singleFileUploadError.style.display = "none";
            singleFileUploadSuccess.innerHTML = "<p>Already Exists. Was Successfully Overwritten.</p>" +
                "<p><b>" + response.fileName + "</b>, DownloadUrl : <a href='" + response.fileDownloadUri + "' target='_blank'>" + response.fileDownloadUri + "</a></p>";
            singleFileUploadSuccess.style.display = "block";
        }
    }
    xhr.send(formData);
}


singleUploadForm.addEventListener('submit', function (event) {
    var files = singleFileUploadInput.files;
    if (files.length === 0) {
        singleFileUploadError.innerHTML = "Please select a photo";
        singleFileUploadError.style.display = "block";
    }
    uploadSingleFile(files[0]);
    event.preventDefault();
}, true);