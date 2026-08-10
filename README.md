# FDT Calculator

**FDT Calculator — Sand Cone Method** is an offline-first field tool designed for QA/QC teams, civil engineering technicians, and construction professionals performing Field Density Tests on road and earthworks projects.

It helps users calculate Field Density Test results quickly and reliably directly on site, without requiring an internet connection.

## 🚧 Key Features

- 📐 Field Density Test calculations using the Sand Cone Method
- 🧪 Support for FDT field test data and calculations
- 📊 Clear calculation results and pass/fail assessment
- 💾 Save and manage test results locally on the device
- 📄 Generate professional PDF reports
- 🏗️ Store project and site information
- 🔄 Backup and restore saved results
- 📱 Mobile-friendly interface for field use
- 📡 Offline-first Progressive Web App (PWA)
- ⚡ Works without an internet connection after installation
- 🔄 Service Worker for offline caching and application updates
- 🧪 Built-in self-test functionality for calculation verification

## 📚 Standards

The calculator is designed around the Sand Cone Method commonly associated with:

- **AASHTO T 191** — Density of Soil In-Place by the Sand-Cone Method
- **ASTM D1556** — Density and Unit Weight of Soil in Place by Sand-Cone Method

> Always verify calculations and acceptance criteria against the applicable project specification and governing standard.

## 🛠️ Technology

Built using:

- HTML5
- CSS3
- JavaScript
- Progressive Web App (PWA)
- Service Worker
- jsPDF
- jsPDF-AutoTable
- Browser Local Storage

## 📱 Offline Use

FDT Calculator is designed for field environments where internet connectivity may be limited or unavailable.

After the application has been loaded and cached, users can:

1. Open the application.
2. Enter FDT field data.
3. Calculate the test result.
4. Save the result.
5. Generate a PDF report.

Internet access is not required for normal calculation and reporting once the required application resources have been cached.

## 💾 Data Storage

Saved project information, settings, and test results are stored locally on the user's device using browser storage.

The application also provides backup and restore functionality to help users preserve their saved records.

> Clearing browser/app data may remove locally stored records. Keep a backup of important results.

## 📄 PDF Reports

The application can generate PDF reports containing FDT calculation information and project/test details.

Reports can be used as part of field QA/QC documentation.

## 🎯 Intended Users

FDT Calculator is intended for:

- Civil engineering technicians
- Laboratory technicians
- QA/QC technicians
- Site engineers
- Road construction teams
- Geotechnical field personnel
- Construction quality-control teams

## 🚀 Getting Started

### Use Online

Open the deployed FDT Calculator application in a supported browser.

### Install as a PWA

On supported browsers:

1. Open the application.
2. Select **Add to Home Screen** or **Install App**.
3. Launch FDT Calculator from your device.

### Run Locally

Clone the repository:

git clone https://github.com/Onesmo3/FDTCalculator.git

### 📁 Project Structure

FDTCalculator/
├── index.html
├── sw.js
├── lib/
│   ├── jspdf.umd.min.js
│   └── jspdf.plugin.autotable.min.js
└── README.md

### 🔒 Data & Privacy

FDT Calculator is designed primarily as a client-side application.
User-entered project information and saved test results are stored locally on the user's device rather than being uploaded to a central database.

### ⚠️ Disclaimer

FDT Calculator is a calculation and field documentation tool.
Users are responsible for verifying input data, calculation results, applicable specifications, and acceptance criteria before using results for engineering decisions or official project documentation.
The application does not replace applicable standards, project specifications, engineering judgment, or QA/QC procedures.

### 📌 Project Status

FDT Calculator is actively being improved with a focus on:
- Reliability
- Offline field use
- Calculation validation
- PDF reporting
- Data protection
- Mobile usability
