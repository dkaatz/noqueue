import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import 'rxjs/add/operator/map';
import { Observable } from 'rxjs/Observable';
import { HttpProvider } from '../providers/http-provider';

/*
  Generated class for the ServicesProvider provider.

  See https://angular.io/docs/ts/latest/guide/dependency-injection.html
  for more info on providers and Angular 2 DI.
*/
@Injectable()
export class ServicesProvider {

  constructor(public http: Http, private httpProvider: HttpProvider) {
  }

  getServicesFor(shopID){
    let route = this.httpProvider.ROUTES.shops + "/" + shopID + "/dienstleistung";
    return this.httpProvider.get(route);
  }

  getService(serviceID, shopID){
    let route = this.httpProvider.ROUTES.shops + "/" + shopID + "/dienstleistung/" + serviceID;
    return this.httpProvider.get(route);
  }

  getAllServiceTypes(){
    return this.httpProvider.get(this.httpProvider.ROUTES.services);
  }

  createService(shopID, service){
    let route = this.httpProvider.ROUTES.shops + "/" + shopID + "/dienstleistung";
    let body = this.mapToExpectedJson(service);
    return this.httpProvider.post(route, body);
  }

  editService(shopID, serviceID, service){
    let route = this.httpProvider.ROUTES.shops + "/" + shopID + "/dienstleistung/" + serviceID;
    let body = this.mapToExpectedJson(service);
    return this.httpProvider.put(route, body);
  }

  private mapToExpectedJson(service){
    return {
      dauer: service.duration,
      typ: service.type,
      beschreibung: service.description
    }
  }
}
