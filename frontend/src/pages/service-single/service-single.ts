import { Component } from '@angular/core';
import { NavController, NavParams } from 'ionic-angular';
import { ShopsProvider } from '../../providers/shops-provider';
import { QueuesProvider } from '../../providers/queues-provider';
import { ServicesProvider } from '../../providers/services-provider';
import { MyQueuePositionPage } from '../my-queue-position/my-queue-position';

/*
  Generated class for the ServiceSingle page.

  See http://ionicframework.com/docs/v2/components/#navigation for more info on
  Ionic pages and navigation.
*/
@Component({
  selector: 'page-service-single',
  templateUrl: 'service-single.html',
  providers: [ ShopsProvider, QueuesProvider, ServicesProvider ],
  entryComponents: [ MyQueuePositionPage ]
})
export class ServiceSinglePage {

  employees = [];
  selectedEmployee = "any";
  service = {};
  shopID: any;
  serviceID: any;
  error = false;
  errorMessage = "";
  queueActive = false;

  constructor(public navCtrl: NavController, public navParams: NavParams, public shopsProvider: ShopsProvider, public queuesProvider: QueuesProvider,
  public servicesProvider: ServicesProvider) {
    this.shopID = this.navParams.get('shopID');
    this.serviceID = this.navParams.get('serviceID');
  }

  ionViewDidLoad() {
    this.reloadData();
  }

  refresh(refresher){
    this.reloadData();

    // @TODO - return a promise in reloadData() and complete the refresher when resolved
    setTimeout(() => {
      refresher.complete();
    }, 1000);
  }

  reloadData(){
    this.error = false;
    this.errorMessage = "";

    console.log("from service page > shopID : serviceID = " + this.shopID + " : " + this.serviceID);

    this.servicesProvider.getService(this.serviceID, this.shopID)
      .subscribe(
        (service) => {
          console.log("service from server: ", service);
          this.service = {
            type: service.typ,
            duration: service.dauer,
            description: service.beschreibung
          }
        },
        (error) => console.log(error)
      );

    this.shopsProvider.getEmployees(this.shopID)
      .subscribe(
        (employees) => {
          let activeEmployees = employees.filter(e => e.anwesend);
          this.employees = activeEmployees;
          this.queueActive = this.employees.length > 0;
        }
      );
  }

  // @TODO
  employeeSelection(){
    // get the next available time slot for this employee
  }

  lineUp(){
    console.log("data at this point: ", this.shopID, this.serviceID, this.selectedEmployee);
    this.queuesProvider.lineup(this.shopID, this.serviceID, this.selectedEmployee)
      .subscribe(
        () => {
          console.log('lined up');
          this.navCtrl.push(MyQueuePositionPage);
        },
        (error) => {
          console.log("error when lining up: ", error);
          this.error = true;
          this.errorMessage = error.message || "Couldn't line up in the queue."
        }
      )
  }


}