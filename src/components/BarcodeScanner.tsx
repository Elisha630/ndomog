import { useState, useRef, useEffect } from "react";
import { Scan, X, Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { toast } from "sonner";
import { BrowserMultiFormatReader, IScannerControls } from "@zxing/browser";
import { DecodeHintType, BarcodeFormat } from "@zxing/library";

interface BarcodeScannerProps {
  onScan: (barcode: string) => void;
}

const BarcodeScanner = ({ onScan }: BarcodeScannerProps) => {
  const [open, setOpen] = useState(false);
  const [scanning, setScanning] = useState(false);
  const [scannedResult, setScannedResult] = useState<string | null>(null);
  const videoRef = useRef<HTMLVideoElement>(null);
  const controlsRef = useRef<IScannerControls | null>(null);

  useEffect(() => {
    if (open) {
      setScannedResult(null);
      startScanning();
    } else {
      stopScanning();
    }

    return () => {
      stopScanning();
    };
  }, [open]);

  const startScanning = async () => {
    setScanning(true);
    try {
      // Configure hints to support multiple barcode formats
      const hints = new Map();
      hints.set(DecodeHintType.POSSIBLE_FORMATS, [
        BarcodeFormat.QR_CODE,
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.CODE_128,
        BarcodeFormat.CODE_39,
        BarcodeFormat.CODE_93,
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
        BarcodeFormat.ITF,
        BarcodeFormat.CODABAR,
        BarcodeFormat.DATA_MATRIX,
        BarcodeFormat.PDF_417,
      ]);
      hints.set(DecodeHintType.TRY_HARDER, true);

      const codeReader = new BrowserMultiFormatReader(hints);
      
      const videoInputDevices = await BrowserMultiFormatReader.listVideoInputDevices();
      
      if (videoInputDevices.length === 0) {
        toast.error("No camera found");
        setOpen(false);
        return;
      }

      // Prefer back camera
      const backCamera = videoInputDevices.find(
        (device) => device.label.toLowerCase().includes("back") || device.label.toLowerCase().includes("rear")
      );
      const selectedDeviceId = backCamera?.deviceId || videoInputDevices[0].deviceId;

      if (videoRef.current) {
        controlsRef.current = await codeReader.decodeFromVideoDevice(
          selectedDeviceId,
          videoRef.current,
          (result) => {
            if (result) {
              const barcode = result.getText();
              const format = result.getBarcodeFormat();
              setScannedResult(barcode);
              setScanning(false);
              toast.success(`Scanned ${BarcodeFormat[format]}: ${barcode}`);
            }
          }
        );
      }
    } catch (error) {
      console.error("Error starting barcode scanner:", error);
      toast.error("Could not access camera. Please check permissions.");
      setOpen(false);
    }
  };

  const stopScanning = () => {
    if (controlsRef.current) {
      controlsRef.current.stop();
      controlsRef.current = null;
    }
    setScanning(false);
  };

  const handleUseBarcode = () => {
    if (scannedResult) {
      onScan(scannedResult);
      setOpen(false);
      setScannedResult(null);
    }
  };

  const handleScanAgain = () => {
    setScannedResult(null);
    startScanning();
  };

  return (
    <>
      <Button
        type="button"
        variant="secondary"
        className="flex-1 h-20 flex-col gap-2"
        onClick={() => setOpen(true)}
      >
        <Scan size={20} />
        <span className="text-xs">Scan Barcode</span>
      </Button>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="bg-popover border-border max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-foreground">
              <Scan className="text-primary" size={20} />
              Scan Barcode
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4">
            {scannedResult ? (
              <div className="space-y-4">
                <div className="p-4 bg-secondary rounded-lg border border-border">
                  <p className="text-xs text-muted-foreground mb-1">Scanned Result</p>
                  <p className="text-lg font-mono font-semibold text-foreground break-all">
                    {scannedResult}
                  </p>
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="secondary"
                    className="flex-1"
                    onClick={handleScanAgain}
                  >
                    <Scan size={16} className="mr-2" />
                    Scan Again
                  </Button>
                  <Button
                    className="flex-1 bg-primary text-primary-foreground"
                    onClick={handleUseBarcode}
                  >
                    <Search size={16} className="mr-2" />
                    Use This Code
                  </Button>
                </div>
              </div>
            ) : (
              <>
                <div className="relative rounded-lg overflow-hidden bg-black aspect-video">
                  {scanning && (
                    <div className="absolute inset-0 flex items-center justify-center z-10">
                      <div className="w-48 h-48 border-2 border-primary rounded-lg animate-pulse" />
                    </div>
                  )}
                  <video
                    ref={videoRef}
                    className="w-full h-full object-cover"
                    autoPlay
                    playsInline
                    muted
                  />
                </div>

                <p className="text-sm text-muted-foreground text-center">
                  Point your camera at a barcode or QR code to scan
                </p>
              </>
            )}

            <Button
              variant="secondary"
              className="w-full"
              onClick={() => setOpen(false)}
            >
              <X size={16} className="mr-2" />
              Cancel
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
};

export default BarcodeScanner;