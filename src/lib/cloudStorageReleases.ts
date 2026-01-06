// Cloud Storage APK hosting (Google Drive / Dropbox)
// For private repositories, use direct download links from these services

export interface CloudRelease {
  version: string;
  releaseDate: string;
  releaseNotes: string;
  downloadUrl: string;
  minAndroidVersion?: string;
}

// Configure your releases here
// To get direct download links:
// 
// GOOGLE DRIVE:
// 1. Upload APK to Google Drive
// 2. Right-click → Share → "Anyone with the link"
// 3. Copy the link (format: https://drive.google.com/file/d/FILE_ID/view)
// 4. Convert to direct download: https://drive.google.com/uc?export=download&id=FILE_ID
//
// DROPBOX:
// 1. Upload APK to Dropbox
// 2. Click "Share" → "Copy link"
// 3. Replace "dl=0" with "dl=1" at the end of the URL
//
export const CLOUD_RELEASES: CloudRelease[] = [
  {
    version: "1.1.0",
    releaseDate: "2025-01-06",
    releaseNotes: `### What's New in v1.1.0
- Improved barcode scanning
- Enhanced offline support
- Bug fixes and performance improvements`,
    downloadUrl: "", // Add your Google Drive or Dropbox direct link here
    minAndroidVersion: "7.0",
  },
  // Add older versions below
  // {
  //   version: "1.0.0",
  //   releaseDate: "2025-01-01",
  //   releaseNotes: "Initial release",
  //   downloadUrl: "YOUR_DIRECT_DOWNLOAD_LINK",
  // },
];

// Convert Google Drive share link to direct download link
export const convertGoogleDriveLink = (shareLink: string): string => {
  // Extract file ID from various Google Drive URL formats
  const patterns = [
    /\/file\/d\/([a-zA-Z0-9_-]+)/,
    /id=([a-zA-Z0-9_-]+)/,
    /\/d\/([a-zA-Z0-9_-]+)/,
  ];

  for (const pattern of patterns) {
    const match = shareLink.match(pattern);
    if (match) {
      return `https://drive.google.com/uc?export=download&id=${match[1]}`;
    }
  }

  return shareLink; // Return original if no pattern matches
};

// Convert Dropbox share link to direct download link
export const convertDropboxLink = (shareLink: string): string => {
  if (shareLink.includes("dropbox.com")) {
    // Replace dl=0 with dl=1 for direct download
    return shareLink.replace(/dl=0/, "dl=1").replace(/\?dl=0/, "?dl=1");
  }
  return shareLink;
};

// Get the latest release from cloud storage
export const getLatestCloudRelease = (): CloudRelease | null => {
  if (CLOUD_RELEASES.length === 0) return null;
  
  // Sort by version (descending) and return the first one
  const sorted = [...CLOUD_RELEASES].sort((a, b) => {
    const aVersion = a.version.split(".").map(Number);
    const bVersion = b.version.split(".").map(Number);
    
    for (let i = 0; i < 3; i++) {
      if ((bVersion[i] || 0) !== (aVersion[i] || 0)) {
        return (bVersion[i] || 0) - (aVersion[i] || 0);
      }
    }
    return 0;
  });
  
  return sorted[0];
};

// Get all cloud releases
export const getAllCloudReleases = (): CloudRelease[] => {
  return [...CLOUD_RELEASES].sort((a, b) => {
    const aVersion = a.version.split(".").map(Number);
    const bVersion = b.version.split(".").map(Number);
    
    for (let i = 0; i < 3; i++) {
      if ((bVersion[i] || 0) !== (aVersion[i] || 0)) {
        return (bVersion[i] || 0) - (aVersion[i] || 0);
      }
    }
    return 0;
  });
};

// Check if cloud storage releases are configured
export const isCloudStorageConfigured = (): boolean => {
  const latest = getLatestCloudRelease();
  return latest !== null && latest.downloadUrl.length > 0;
};

// Detect and convert any cloud storage link to direct download
export const toDirectDownloadLink = (url: string): string => {
  if (url.includes("drive.google.com")) {
    return convertGoogleDriveLink(url);
  }
  if (url.includes("dropbox.com")) {
    return convertDropboxLink(url);
  }
  return url;
};
