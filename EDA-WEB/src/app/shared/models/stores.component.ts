import { STORES } from './mock-stores';
import {Component, OnInit} from '@angular/core';
import {Store} from './store.model';

@Component({
  selector: 'app-stores',
  templateUrl: './stores.component.html',
  styleUrls: ['./stores.component.css']
})

export class StoresComponent implements OnInit {
  stores = STORES;
  selectedStore: Store;

  constructor() {

  }

  ngOnInit(): void {
  }

  onSelect(store: Store):
    void {
    this.selectedStore = store;
  }
}
