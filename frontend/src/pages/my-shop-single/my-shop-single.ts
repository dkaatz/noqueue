import { Component } from '@angular/core';
import { NavController, NavParams } from 'ionic-angular';
import { ShopsProvider } from '../../providers/shops-provider';
import { ServicesProvider } from '../../providers/services-provider';
import { AuthenticationProvider } from '../../providers/authentication-provider';
import { ShopInfoPage } from '../shop-info/shop-info';
import { ServiceInfoPage } from '../service-info/service-info';
import { CoworkersPage } from '../coworkers/coworkers';
import {QueuesProvider} from "../../providers/queues-provider";

/*
  Generated class for the MyShopSingle page.

  See http://ionicframework.com/docs/v2/components/#navigation for more info on
  Ionic pages and navigation.
*/
@Component({
  selector: 'page-my-shop-single',
  templateUrl: 'my-shop-single.html',
  providers: [ShopsProvider, ServicesProvider, QueuesProvider],
  entryComponents: [ ShopInfoPage, ServiceInfoPage, CoworkersPage ]
})
export class MyShopSinglePage {

// declare variables used by the HTML template (ViewModel)

  isAnwesend: boolean = false;
  shopID: number;
  isLeiter: boolean = false;
  shop = {
    name: string = "",
    address: string = "",
    phone: string = "",
    email: string = "",
    openingHours: string = ""
  };
  managers: any = [];
  employees: any = [];
  currentManagerWorking: boolean = false;
  hasOwnQueueToggle: boolean = false;
  services: any = [];
  error: boolean = false;
  errorMessage: string = "";
  queue: any = [];
  clients: any = [];
  firstClient: string = "";
  firstStarted: boolean = false;

// constructor and lifecycle-events (chronological order)

  constructor(public navCtrl: NavController, public navParams: NavParams, public shopsProvider: ShopsProvider, public servicesProvider: ServicesProvider,
  public auth: AuthenticationProvider, public queuesProvider: QueuesProvider) {
    this.shopID = this.navParams.get('shopID');
    this.isLeiter = this.navParams.get('isLeiter');
    this.isAnwesend = this.navParams.get('isAnwesend');
  }

  ionViewDidLoad() : void{
    this.reloadData();
  }

  ionViewWillLeave() : void{
    // send changes of the toggle only when leaving the page
    if(this.hasOwnQueueToggle !== this.currentManagerWorking){
      // this.shopsProvider.managerWorking(userID, shopID, bool)..
    }
  }

// ViewModel logic (working with the data)

  registerError(message: string) : void{
    this.error = true;
    this.errorMessage = message;
  }

  reloadData() : void{
    this.error = false;
    this.errorMessage = "";

    this.shopsProvider.getShop(this.shopID)
      .subscribe(
        (shop) => {
          console.log("GET Shop: ", shop);
          this.shop = {
            name: shop.name,
            phone: shop.tel,
            email: shop.kontaktEmail,
            openingHours: shop.oeffnungszeiten,
            address: shop.adresse.strasse + " " + shop.adresse.hausNummer + ", " + shop.adresse.plz + shop.adresse.stadt
          }
        },
        (error) => this.registerError(error.message || "Coulnd't get this shop from the server")
      );

    if(this.isLeiter) {
      this.shopsProvider.getEmployees(this.shopID)
        .subscribe(
          (employees) => {
            console.log("GET Employees: ", employees);
            this.employees = employees;
          },
          (error) => this.registerError(error.message || "Something went wrong")
        );

      this.shopsProvider.getManagers(this.shopID)
        .subscribe(
          (managers) => {
            console.log("GET Managers: ", managers);
            this.managers = managers;
          },
          (error) => this.registerError(error.message || "Something went wrong")
        );

      this.servicesProvider.getServicesFor(this.shopID)
        .subscribe(
          (services) => this.services = services,
          (error) => this.registerError(error.message || "Something went wrong")
        )
    } else {
      this.shopsProvider.getQueueFor(this.shopID)
        .subscribe(
          (queue) => {
            console.log("queue: ", queue);
            this.queue = queue;
            this.clients = queue.wsps;
            if(this.clients.length > 0){
              this.firstClient = this.clients[0].anwender.nutzerName;
            }
          },
            (error) => this.registerError(error.message || "Something went wrong")
        )
    }
  }

  // start(){
  //   this.firstStarted = true;
  // }
  //
  // end(){
  //   this.firstStarted = false;
  //   this.reloadData();
  // }

// ViewController logic (reacting to events)

  switchAttendance() : void{
    this.queuesProvider.changeAttendance(this.shopID, !this.isAnwesend).subscribe(
      () => { this.isAnwesend = !this.isAnwesend},
      (error) => this.registerError(error || "Couldn't change attendance!")
    )
  }


  fireManager(slidingItem: any, userID: number) : void{
    slidingItem.close();
    this.shopsProvider.fireManager(userID, this.shopID)
      .subscribe(
        () => this.reloadData(),
        (error) => {
          let jsonError = JSON.parse(error._body);
          console.log("Error while firing manager: ", jsonError);
          this.registerError(jsonError.message);
        }
      )
  }

  fireEmployee(slidingItem: any, userID: number) : void{
    slidingItem.close();
    this.shopsProvider.fireEmployee(userID, this.shopID)
      .subscribe(
        () => this.reloadData(),
        (error) => {
          let jsonError = JSON.parse(error._body);
          console.log("Error while firing employee: ", jsonError);
          this.registerError(jsonError.message);
        }
      )
  }

  startWorkOn(wspId: number) : void{
    this.queuesProvider.startWorkOn(this.shopID, wspId)
      .subscribe(
        () => this.reloadData(),
        (error) => {
          let jsonError = JSON.parse(error._body);
          console.log("Error while firing employee: ", jsonError);
          this.registerError(jsonError.message);
        }
      )
  }

  finishWorkOn(wspId: number) : void{
    this.queuesProvider.finishWorkOn(this.shopID, wspId)
      .subscribe(
        () => this.reloadData(),
        (error) => {
          let jsonError = JSON.parse(error._body);
          console.log("Error while firing employee: ", jsonError);
          this.registerError(jsonError.message);
        }
      )
  }

  editShopInfo() : void{
    if(this.isLeiter)
      this.navCtrl.push(ShopInfoPage, {newShop: false, shopID: this.shopID});
  }

  showService(serviceID: number) : void{
    let service = this.services.filter(s => s.id == serviceID)[0];
    console.log("will show: ", service);
    this.navCtrl.push(ServiceInfoPage, {newShop: false, shopID: this.shopID, newService: false, serviceID: serviceID, service: service});
  }

  deleteService(serviceID: number) : void{
    console.log("will delete: " + serviceID);
    //
  }

  createService() : void{
    console.log("will create");
    this.navCtrl.push(ServiceInfoPage, {newShop: false, shopID: this.shopID, newService: true, serviceID: -1, service: undefined});
  }

  addCoworkers() : void{
    this.navCtrl.push(CoworkersPage, {newShop: false, shopID: this.shopID});
  }


}
