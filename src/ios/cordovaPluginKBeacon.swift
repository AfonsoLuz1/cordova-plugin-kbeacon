import Foundation
import kbeaconlib2

@objc(CordovaPluginKBeacon)
public class CordovaPluginKBeacon: CDVPlugin, KBeaconMgrDelegate {

    var mBeaconsMgr: KBeaconsMgr?

    // Thread-safe storage
    static private let beaconAccessQueue = DispatchQueue(label: "com.heroesde4patas.kbeacon.sync")
    static private var _mBeaconsDictory = [String: [String]]()

    static var mBeaconsDictory: [String: [String]] {
        get {
            return beaconAccessQueue.sync { _mBeaconsDictory }
        }
        set {
            beaconAccessQueue.sync { _mBeaconsDictory = newValue }
        }
    }

    static var mBeaconsArray: [[String]] {
        return beaconAccessQueue.sync {
            Array(_mBeaconsDictory.values)
        }
    }

    public override func pluginInitialize() {
        super.pluginInitialize()
        print("Plugin inicializado")

        mBeaconsMgr = KBeaconsMgr.sharedBeaconManager
        mBeaconsMgr?.delegate = self
    }

    public func onCentralBleStateChange(newState: BLECentralMgrState) {
        if newState == .PowerOn {
            print("Central BLE state is PowerOn")
        }
    }

    public func onBeaconDiscovered(beacons: [KBeacon]) {
        for beacon in beacons {
            printScanPacket(beacon)
        }
    }

    private func printScanPacket(_ advBeacon: KBeacon) {
        guard let allAdvPackets = advBeacon.allAdvPackets else {
            return
        }

        for advPacket in allAdvPackets {
            switch advPacket.getAdvType() {
            case .IBeacon:
                if let iBeaconAdv = advPacket as? KBAdvPacketIBeacon,
                   let uuid = advBeacon.uuidString {

                    let kbArray: [String] = [
                        String(advBeacon.rssi),
                        String(iBeaconAdv.refTxPower),
                        uuid,
                        String(iBeaconAdv.minorID),
                        String(iBeaconAdv.majorID)
                    ]

                    CordovaPluginKBeacon.beaconAccessQueue.sync {
                        CordovaPluginKBeacon._mBeaconsDictory[String(iBeaconAdv.minorID)] = kbArray
                    }
                }

            default:
                print("Unknown advertisement packet")
            }
        }

        // Remove buffered packet
        advBeacon.removeAdvPacket()
    }

    @objc
    func getDiscoveredDevicesiOS(_ command: CDVInvokedUrlCommand) {
        let beacons = CordovaPluginKBeacon.mBeaconsArray

        let pluginResult: CDVPluginResult
        if !beacons.isEmpty {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: beacons)
        } else {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No devices found.")
        }

        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func startScaniOS(_ command: CDVInvokedUrlCommand) {
        guard let scanResult = mBeaconsMgr?.startScanning() else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Beacon manager not initialized.")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        let pluginResult: CDVPluginResult
        if scanResult {
            print("Started scanning for beacons")
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Scanning started")
        } else {
            print("Failed to start scanning")
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Failed to start scanning")
        }

        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func stopScanning() {
        if let mgr = mBeaconsMgr {
            mgr.stopScanning()
            print("Scanning stopped")
        } else {
            print("Cannot stop scanning: manager not initialized")
        }
    }
}
